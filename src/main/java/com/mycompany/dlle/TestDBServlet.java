package com.mycompany.dlle;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/testdb")
public class TestDBServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        try (Connection con = DBConnection.getConnection()) {

            PrintWriter out = response.getWriter();

            if (con != null) {
                out.println("<h2>Database connected successfully!</h2>");
            }

        } catch (Exception e) {

    PrintWriter out = response.getWriter();
    out.println("<h2>Database connection failed!</h2>");
    out.println("<p>" + e.getMessage() + "</p>");

    e.printStackTrace(out);
}
    }
}