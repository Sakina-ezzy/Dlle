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

@WebServlet("/teacherAttendance")
public class TeacherAttendanceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("teacherLoggedIn") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().print(
                    "{\"status\":\"UNAUTHORIZED\"}"
            );

            return;
        }

        String eventIdText =
                request.getParameter("eventId");

        String year =
                request.getParameter("year");

        if (eventIdText == null
                || eventIdText.trim().isEmpty()) {

            response.getWriter().print(
                    "{\"status\":\"INVALID\"}"
            );

            return;
        }

        if (year == null
                || year.trim().isEmpty()) {

            year = "ALL";
        }

        int eventId;

        try {

            eventId =
                    Integer.parseInt(
                            eventIdText.trim()
                    );

        } catch (NumberFormatException e) {

            response.getWriter().print(
                    "{\"status\":\"INVALID\"}"
            );

            return;
        }

        String sql =
                "SELECT "
                + "a.attendance_id, "
                + "a.attendance_time, "
                + "a.hours_awarded, "
                + "a.year, "
                + "a.photo_path, "
                + "s.student_id, "
                + "s.student_name, "
                + "e.stream "
                + "FROM attendance a "
                + "JOIN students s "
                + "ON a.student_id = s.student_id "
                + "JOIN events e "
                + "ON a.event_id = e.event_id "
                + "WHERE a.event_id = ? ";

        if (!year.equals("ALL")) {

            sql +=
                    "AND a.year = ? ";

        }

        sql +=
                "ORDER BY a.attendance_time ASC";

        StringBuilder json =
                new StringBuilder();

        json.append("{");
        json.append("\"status\":\"SUCCESS\",");
        json.append("\"total\":");

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            if (!year.equals("ALL")) {

                ps.setString(2, year);

            }

            try (ResultSet rs =
                         ps.executeQuery()) {

                StringBuilder records =
                        new StringBuilder();

                records.append("[");

                int count = 0;

                while (rs.next()) {

                    if (count > 0) {
                        records.append(",");
                    }

                    count++;

                    int attendanceId =
                            rs.getInt(
                                    "attendance_id"
                            );

                    String attendanceTime =
                            rs.getString(
                                    "attendance_time"
                            );

                    int hours =
                            rs.getInt(
                                    "hours_awarded"
                            );

                    String studentId =
                            rs.getString(
                                    "student_id"
                            );

                    String studentName =
                            rs.getString(
                                    "student_name"
                            );

                    String studentYear =
                            rs.getString(
                                    "year"
                            );

                    String stream =
                            rs.getString(
                                    "stream"
                            );

                    records.append("{");

                    records.append(
                            "\"attendanceId\":"
                    )
                    .append(attendanceId)
                    .append(",");

                    records.append(
                            "\"studentId\":\""
                    )
                    .append(
                            escapeJson(studentId)
                    )
                    .append("\",");

                    records.append(
                            "\"studentName\":\""
                    )
                    .append(
                            escapeJson(studentName)
                    )
                    .append("\",");

                    records.append(
                            "\"stream\":\""
                    )
                    .append(
                            escapeJson(stream)
                    )
                    .append("\",");

                    records.append(
                            "\"year\":\""
                    )
                    .append(
                            escapeJson(studentYear)
                    )
                    .append("\",");

                    records.append(
                            "\"attendanceTime\":\""
                    )
                    .append(
                            escapeJson(attendanceTime)
                    )
                    .append("\",");

                    records.append(
                            "\"hours\":"
                    )
                    .append(hours);

                    records.append("}");
                }

                records.append("]");

                json.append(count)
                    .append(",");

                json.append(
                        "\"attendance\":"
                )
                .append(records);

                json.append("}");

                response.getWriter().print(
                        json.toString()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().print(
                    "{\"status\":\"ERROR\"}"
            );
        }
    }

    private String escapeJson(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}