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

@WebServlet("/createEvent")
public class CreateEventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("teacherLoggedIn") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().print("UNAUTHORIZED");
            return;
        }

        String teacherName =
                request.getParameter("teacherName");

        String title =
                request.getParameter("title");

        String description =
                request.getParameter("description");

        String eventDate =
                request.getParameter("eventDate");

        String hoursText =
                request.getParameter("hours");

        if (teacherName == null
                || teacherName.trim().isEmpty()
                || title == null
                || title.trim().isEmpty()
                || eventDate == null
                || eventDate.trim().isEmpty()
                || hoursText == null
                || hoursText.trim().isEmpty()) {

            response.getWriter().print("INVALID");
            return;
        }

        int hours;

        try {

            hours = Integer.parseInt(
                    hoursText.trim()
            );

        } catch (NumberFormatException e) {

            response.getWriter().print("INVALID");
            return;
        }

        if (hours <= 0) {

            response.getWriter().print("INVALID");
            return;
        }

        String sql =
        "INSERT INTO events "
        + "(title, description, event_date, hours, "
        + "created_by, stream, attendance_status, archived) "
        + "VALUES (?, ?, ?, ?, ?, ?, 'CLOSED', FALSE)";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, title.trim());

            ps.setString(2,
                    description == null
                    ? ""
                    : description.trim());

            ps.setString(3, eventDate.trim());

            ps.setInt(4, hours);

            // Teacher enters this name
            ps.setString(5, teacherName.trim());

            // Current project stream
            ps.setString(6, "IT");

            ps.executeUpdate();

            response.getWriter().print("SUCCESS");

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().print("ERROR");
        }
    }
}