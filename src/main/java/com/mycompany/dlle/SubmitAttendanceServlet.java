package com.mycompany.dlle;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@WebServlet("/submitAttendance/*")
public class SubmitAttendanceServlet extends HttpServlet {

    private static final String PHOTO_BASE_PATH =
            "C:\\Users\\admin\\Pictures\\attendance-photos";

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.getWriter().print("LOGIN_REQUIRED");
            return;
        }

        String studentId = (String) session.getAttribute("studentId");

        // Get event ID from URL
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            response.getWriter().print("EVENT_ID_MISSING");
            return;
        }

        String eventIdText = pathInfo.substring(1);

        int eventId;

        try {
            eventId = Integer.parseInt(eventIdText);
        } catch (NumberFormatException e) {
            response.getWriter().print("INVALID_EVENT_ID");
            return;
        }

        String capturedPhoto = request.getParameter("capturedPhoto");

        if (capturedPhoto == null || capturedPhoto.trim().isEmpty()) {
            response.getWriter().print("PHOTO_MISSING");
            return;
        }

        Path imagePath = null;

        try (Connection con = DBConnection.getConnection()) {

            // -------------------------------------------------
            // 1. Check whether student is registered
            // -------------------------------------------------

            String registrationSql =
                    "SELECT registration_id "
                    + "FROM registrations "
                    + "WHERE event_id = ? AND student_id = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(registrationSql)) {

                ps.setInt(1, eventId);
                ps.setString(2, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        response.getWriter().print("NOT_REGISTERED");
                        return;
                    }
                }
            }

            // -------------------------------------------------
            // 2. Get event details
            // -------------------------------------------------

            int eventHours;

            String eventSql =
                    "SELECT hours, attendance_status "
                    + "FROM events "
                    + "WHERE event_id = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(eventSql)) {

                ps.setInt(1, eventId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        response.getWriter().print("EVENT_NOT_FOUND");
                        return;
                    }

                    eventHours = rs.getInt("hours");

                    String attendanceStatus =
                            rs.getString("attendance_status");

                    if (!"OPEN".equalsIgnoreCase(attendanceStatus)) {
                        response.getWriter().print("ATTENDANCE_CLOSED");
                        return;
                    }
                }
            }

            // -------------------------------------------------
            // 3. Check whether attendance already submitted
            // -------------------------------------------------

            String duplicateSql =
                    "SELECT attendance_id "
                    + "FROM attendance "
                    + "WHERE event_id = ? AND student_id = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(duplicateSql)) {

                ps.setInt(1, eventId);
                ps.setString(2, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        response.getWriter().print("ALREADY_SUBMITTED");
                        return;
                    }
                }
            }

            // -------------------------------------------------
            // 4. Get student's academic year
            // -------------------------------------------------

            String year = null;

            String studentSql =
                    "SELECT year "
                    + "FROM students "
                    + "WHERE student_id = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(studentSql)) {

                ps.setString(1, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        year = rs.getString("year");
                    }
                }
            }

            // -------------------------------------------------
            // 5. Decode Base64 photo
            // -------------------------------------------------

            String base64 = capturedPhoto;

            if (base64.contains(",")) {
                base64 = base64.substring(
                        base64.indexOf(",") + 1
                );
            }

            byte[] imageBytes;

            try {
                imageBytes = Base64.getDecoder().decode(base64);
            } catch (IllegalArgumentException e) {
                response.getWriter().print("INVALID_PHOTO");
                return;
            }

            // -------------------------------------------------
            // 6. Create event folder
            // -------------------------------------------------

            Path baseDir =
                    Paths.get(PHOTO_BASE_PATH);

            Path eventDir =
                    baseDir.resolve("Event_" + eventId);

            Files.createDirectories(eventDir);

            // -------------------------------------------------
            // 7. Create unique JPG filename
            // -------------------------------------------------

            String safeStudentId =
                    studentId.replaceAll(
                            "[^a-zA-Z0-9_-]",
                            "_"
                    );

            String fileName =
                    "student_"
                    + safeStudentId
                    + "_"
                    + UUID.randomUUID()
                    + ".jpg";

            imagePath =
                    eventDir.resolve(fileName);

            // -------------------------------------------------
            // 8. Save actual image file
            // -------------------------------------------------

            Files.write(imagePath, imageBytes);

            // Relative path stored in MySQL
            String photoPath =
                    "Event_" + eventId + "/" + fileName;

            // -------------------------------------------------
            // 9. Save attendance + photo path
            // -------------------------------------------------

            String insertSql =
                    "INSERT INTO attendance "
                    + "(event_id, student_id, photo_path, "
                    + "hours_awarded, year) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps =
                         con.prepareStatement(insertSql)) {

                ps.setInt(1, eventId);
                ps.setString(2, studentId);
                ps.setString(3, photoPath);
                ps.setInt(4, eventHours);
                ps.setString(5, year);

                ps.executeUpdate();
            }

            // -------------------------------------------------
            // 10. Success
            // -------------------------------------------------

            response.getWriter().print(
                    "SUCCESS"
            );

        } catch (Exception e) {

            // If database insertion failed after the image
            // was created, remove the orphan image.
            if (imagePath != null) {
                try {
                    Files.deleteIfExists(imagePath);
                } catch (IOException ignored) {
                }
            }

            e.printStackTrace();
            response.getWriter().print("ERROR");
        }
    }
}