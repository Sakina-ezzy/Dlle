package com.mycompany.dlle;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/studentDetails")
public class StudentDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        if (session == null || session.getAttribute("studentId") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            out.println("{\"error\":\"Student not logged in\"}");
            return;
        }

        String studentId = (String) session.getAttribute("studentId");
        String studentName = (String) session.getAttribute("studentName");
        String rollNo = (String) session.getAttribute("rollNo");
        String year = (String) session.getAttribute("year");
        Integer totalHours = (Integer) session.getAttribute("totalHours");

        if (totalHours == null) {
            totalHours = 0;
        }

        out.println("{");
        out.println("\"studentId\":\"" + escapeJson(studentId) + "\",");
        out.println("\"name\":\"" + escapeJson(studentName) + "\",");
        out.println("\"rollNo\":\"" + escapeJson(rollNo) + "\",");
        out.println("\"year\":\"" + escapeJson(year) + "\",");
        out.println("\"hours\":" + totalHours);
        out.println("}");
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