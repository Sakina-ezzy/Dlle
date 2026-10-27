package com.mycompany.dlle;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/studentSignup")
public class StudentSignupServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String studentId = request.getParameter("studentId");
        String rollNo = request.getParameter("rollNo");
        String year = request.getParameter("year");
        String password = request.getParameter("password");

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try (Connection con = DBConnection.getConnection()) {

            // Check if account already exists
            String checkSql =
                    "SELECT student_id FROM students WHERE student_id = ?";

            try (PreparedStatement checkPs =
                         con.prepareStatement(checkSql)) {

                checkPs.setString(1, studentId);

                try (ResultSet rs = checkPs.executeQuery()) {

                    if (rs.next()) {
                        response.getWriter().print(
                                "ACCOUNT_EXISTS"
                        );
                        return;
                    }
                }
            }

            // Create account
            String sql =
                    "INSERT INTO students "
                    + "(student_id, student_name, roll_no, year, password, total_hours) "
                    + "VALUES (?, ?, ?, ?, ?, 0)";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(1, studentId);
                ps.setString(2, name);
                ps.setString(3, rollNo);
                ps.setString(4, year);
                ps.setString(5, password);

                ps.executeUpdate();
            }

            response.getWriter().print(
                    "SUCCESS"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().print(
                    "ERROR"
            );
        }
    }
}