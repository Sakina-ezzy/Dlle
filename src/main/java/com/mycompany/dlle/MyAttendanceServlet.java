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

@WebServlet("/myAttendance")
public class MyAttendanceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out =
                response.getWriter();

        /* Check student login */

        if (session == null ||
                session.getAttribute("studentId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            out.println(
                    "{\"error\":\"Student not logged in\"}"
            );

            return;
        }


        String studentId =
                (String) session.getAttribute("studentId");


        /* Get attendance records */

        String sql =
                "SELECT a.attendance_id, "
                + "a.event_id, "
                + "a.attendance_time, "
                + "a.hours_awarded, "
                + "a.year, "
                + "e.title, "
                + "e.description, "
                + "e.event_date "
                + "FROM attendance a "
                + "JOIN events e "
                + "ON a.event_id = e.event_id "
                + "WHERE a.student_id = ? "
                + "ORDER BY a.attendance_time DESC";


        try (Connection con =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     con.prepareStatement(sql)) {


            ps.setString(1, studentId);


            try (ResultSet rs =
                         ps.executeQuery()) {

                out.println("[");

                boolean first = true;


                while (rs.next()) {

                    if (!first) {
                        out.println(",");
                    }


                    out.println("{");

                    out.println(
                            "\"attendanceId\":"
                            + rs.getInt("attendance_id")
                            + ","
                    );

                    out.println(
                            "\"eventId\":"
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
                            "\"attendanceTime\":\""
                            + escapeJson(
                                    rs.getString(
                                            "attendance_time"
                                    )
                            )
                            + "\","
                    );

                    out.println(
                            "\"hours\":"
                            + rs.getInt(
                                    "hours_awarded"
                            )
                            + ","
                    );

                    out.println(
                            "\"year\":\""
                            + escapeJson(
                                    rs.getString("year")
                            )
                            + "\""
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

            out.println(
                    "{\"error\":\"Unable to load attendance\"}"
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