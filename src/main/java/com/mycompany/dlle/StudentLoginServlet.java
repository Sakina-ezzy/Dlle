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

@WebServlet("/studentLogin")
public class StudentLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String studentId = request.getParameter("studentId");
        String password = request.getParameter("password");

        String sql = "SELECT student_name, roll_no, year, total_hours "
                + "FROM students "
                + "WHERE student_id = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String studentName = rs.getString("student_name");
                    String rollNo = rs.getString("roll_no");
                    String year = rs.getString("year");
                    int totalHours = rs.getInt("total_hours");

                    request.getSession().setAttribute("studentId", studentId);
                    request.getSession().setAttribute("studentName", studentName);
                    request.getSession().setAttribute("rollNo", rollNo);
                    request.getSession().setAttribute("year", year);
                    request.getSession().setAttribute("totalHours", totalHours);

                    response.sendRedirect("student-dashboard.html");

                } else {

                    response.setContentType("text/html");

                    response.getWriter().println(
                            "<h2>Login failed!</h2>"
                    );

                    response.getWriter().println(
                            "<p>Incorrect Student ID or password.</p>"
                    );
                }
            }

        } catch (Exception e) {

            response.setContentType("text/html");

            response.getWriter().println(
                    "<h2>Login error!</h2>"
            );

            response.getWriter().println(
                    "<p>" + e.getMessage() + "</p>"
            );

            e.printStackTrace(response.getWriter());
        }
    }
}