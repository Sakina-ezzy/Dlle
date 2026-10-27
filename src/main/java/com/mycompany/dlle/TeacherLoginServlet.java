package com.mycompany.dlle;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/teacherLogin")
public class TeacherLoginServlet extends HttpServlet {

    // Common teacher login
    private static final String TEACHER_USERNAME = "teacher";
    private static final String TEACHER_PASSWORD = "dulle123";

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String username =
                request.getParameter("username");

        String password =
                request.getParameter("password");

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");


        if (username == null || password == null) {

            response.getWriter().print("INVALID");

            return;
        }


        if (TEACHER_USERNAME.equals(username.trim())
                && TEACHER_PASSWORD.equals(password)) {

            // Create/login session
            request.getSession(true)
                    .setAttribute("teacherLoggedIn", true);

            response.getWriter().print("SUCCESS");

        } else {

            response.getWriter().print("INVALID");
        }
    }
}