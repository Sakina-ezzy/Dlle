package com.mycompany.dlle;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class AttendancePhotoServlet extends HttpServlet {

    private static final String PHOTO_BASE_DIRECTORY =
            "C:\\Users\\admin\\Pictures\\attendance-photos";

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("teacherLoggedIn") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String attendanceId =
                request.getParameter("attendanceId");

        if (attendanceId == null ||
            attendanceId.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

        String photoPath = null;

        String sql =
                "SELECT photo_path " +
                "FROM attendance " +
                "WHERE attendance_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(attendanceId));

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    photoPath =
                            rs.getString("photo_path");
                }
            }

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            return;

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            return;
        }

        if (photoPath == null ||
            photoPath.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        /*
         * The database stores a relative path.
         * Example:
         *
         * Event_1/student_123_xxxxx.jpg
         */

        File baseDirectory =
                new File(PHOTO_BASE_DIRECTORY);

        File photoFile =
                new File(
                        baseDirectory,
                        photoPath
                );

        /*
         * Security check:
         * Make sure the requested file
         * stays inside the attendance
         * photo directory.
         */

        try {

            String basePath =
                    baseDirectory
                            .getCanonicalPath();

            String filePath =
                    photoFile
                            .getCanonicalPath();

            if (!filePath.startsWith(
                    basePath + File.separator)) {

                response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN);

                return;
            }

        } catch (IOException e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            return;
        }

        if (!photoFile.exists() ||
            !photoFile.isFile()) {

            response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        response.setContentType(
                "image/jpeg");

        String download =
                request.getParameter("download");

        if ("true".equalsIgnoreCase(download)) {

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"attendance-photo.jpg\""
            );

        } else {

            response.setHeader(
                    "Content-Disposition",
                    "inline"
            );
        }

        response.setContentLengthLong(
                photoFile.length()
        );

        try (OutputStream out =
                     response.getOutputStream()) {

            Files.copy(
                    photoFile.toPath(),
                    out
            );
        }
    }
}