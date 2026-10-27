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

@WebServlet("/teacherRegistrations")
public class TeacherRegistrationsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

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


        if (year == null
                || year.trim().isEmpty()) {

            year = "ALL";

        } else {

            year = year.trim().toUpperCase();

        }


        String sql =
                "SELECT "
                + "r.registration_id, "
                + "r.registration_date, "
                + "s.student_id, "
                + "s.student_name, "
                + "s.year, "
                + "e.stream "
                + "FROM registrations r "
                + "JOIN students s "
                + "ON r.student_id = s.student_id "
                + "JOIN events e "
                + "ON r.event_id = e.event_id "
                + "WHERE r.event_id = ? ";


        if (!year.equals("ALL")) {

            sql +=
                    "AND s.year = ? ";

        }


        sql +=
                "ORDER BY r.registration_date ASC";


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


                /*
                 * First collect the records
                 * so we can calculate total.
                 */

                StringBuilder records =
                        new StringBuilder();

                int total = 0;

                boolean first = true;


                while (rs.next()) {

                    if (!first) {

                        records.append(",");

                    }

                    first = false;

                    total++;


                    String studentId =
                            rs.getString("student_id");

                    String studentName =
                            rs.getString("student_name");

                    String stream =
                            rs.getString("stream");

                    String studentYear =
                            rs.getString("year");

                    String registrationDate =
                            rs.getString(
                                    "registration_date"
                            );


                    records.append("{");


                    records.append(
                            "\"studentId\":\""
                    );

                    records.append(
                            escapeJson(studentId)
                    );

                    records.append("\",");


                    records.append(
                            "\"studentName\":\""
                    );

                    records.append(
                            escapeJson(studentName)
                    );

                    records.append("\",");


                    records.append(
                            "\"stream\":\""
                    );

                    records.append(
                            escapeJson(stream)
                    );

                    records.append("\",");


                    records.append(
                            "\"year\":\""
                    );

                    records.append(
                            escapeJson(studentYear)
                    );

                    records.append("\",");


                    records.append(
                            "\"registrationDate\":\""
                    );

                    records.append(
                            escapeJson(registrationDate)
                    );

                    records.append("\"");


                    records.append("}");

                }


                /*
                 * Finish JSON response
                 */

                json.setLength(0);

                json.append("{");

                json.append(
                        "\"status\":\"SUCCESS\","
                );

                json.append(
                        "\"total\":"
                );

                json.append(total);

                json.append(",");

                json.append(
                        "\"registrations\":["
                );

                json.append(records);

                json.append("]");

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