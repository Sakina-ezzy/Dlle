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

@WebServlet("/archiveEvent")
public class ArchiveEventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("teacherLoggedIn") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().print("UNAUTHORIZED");
            return;
        }

        String eventIdText =
                request.getParameter("eventId");

        if (eventIdText == null
                || eventIdText.trim().isEmpty()) {

            response.getWriter().print("INVALID");
            return;
        }

        int eventId;

        try {

            eventId = Integer.parseInt(
                    eventIdText.trim()
            );

        } catch (NumberFormatException e) {

            response.getWriter().print("INVALID");
            return;
        }

        String sql =
                "UPDATE events "
                + "SET archived = TRUE "
                + "WHERE event_id = ?";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                response.getWriter().print("SUCCESS");

            } else {

                response.getWriter().print("NOT_FOUND");
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().print("ERROR");
        }
    }
}