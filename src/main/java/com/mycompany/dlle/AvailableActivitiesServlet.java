package com.mycompany.dlle;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/availableActivities")
public class AvailableActivitiesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("studentId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().println(
                    "{\"error\":\"Student not logged in\"}"
            );

            return;
        }

        String studentId =
                (String) session.getAttribute("studentId");


        String sql =
                "SELECT e.event_id, "
                + "e.title, "
                + "e.description, "
                + "e.event_date, "
                + "e.hours, "
                + "e.stream, "
                + "e.attendance_status, "
                + "r.registration_id "
                + "FROM events e "
                + "LEFT JOIN registrations r "
                + "ON e.event_id = r.event_id "
                + "AND r.student_id = ? "
                + "ORDER BY e.event_date ASC";


        try (Connection con =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                PrintWriter out =
                        response.getWriter();

                out.println("[");

                boolean first = true;


                while (rs.next()) {

                    if (!first) {
                        out.println(",");
                    }


                    boolean registered =
                            rs.getObject("registration_id") != null;


                    out.println("{");

                    out.println(
                            "\"id\":"
                            + rs.getInt("event_id")
                            + ","
                    );

                    out.println(
                            "\"title\":\""
                            + escapeJson(
                                    rs.getString("title")
                            )
                            + "\","
                    );

                    out.println(
                            "\"description\":\""
                            + escapeJson(
                                    rs.getString("description")
                            )
                            + "\","
                    );

                    out.println(
                            "\"date\":\""
                            + rs.getDate("event_date")
                            + "\","
                    );

                    out.println(
                            "\"hours\":"
                            + rs.getInt("hours")
                            + ","
                    );

                    out.println(
                            "\"stream\":\""
                            + escapeJson(
                                    rs.getString("stream")
                            )
                            + "\","
                    );

                    out.println(
                            "\"attendanceStatus\":\""
                            + escapeJson(
                                    rs.getString(
                                            "attendance_status"
                                    )
                            )
                            + "\","
                    );

                    out.println(
                            "\"registered\":"
                            + registered
                    );

                    out.println("}");

                    first = false;
                }


                out.println("]");
            }


        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().println(
                    "{\"error\":\"Unable to load activities\"}"
            );
        }
    }


    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}