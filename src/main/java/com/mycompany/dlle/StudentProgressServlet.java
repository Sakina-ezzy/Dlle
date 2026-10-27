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

@WebServlet("/studentProgress")
public class StudentProgressServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

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

        String sql =
                "SELECT COALESCE(SUM(hours_awarded), 0) AS total_hours "
                + "FROM attendance "
                + "WHERE student_id = ?";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    int totalHours =
                            rs.getInt("total_hours");

                    int targetHours = 120;

                    int percentage =
                            (int) Math.round(
                                    (totalHours * 100.0)
                                    / targetHours
                            );

                    if (percentage > 100) {
                        percentage = 100;
                    }

                    out.println("{");

                    out.println(
                            "\"hours\":" + totalHours + ","
                    );

                    out.println(
                            "\"target\":120,"
                    );

                    out.println(
                            "\"percentage\":" + percentage
                    );

                    out.println("}");
                }
            }

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.println(
                    "{\"error\":\"Unable to load progress\"}"
            );
        }
    }
}