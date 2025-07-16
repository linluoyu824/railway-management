package com.railway.managementsystem.config;

import com.railway.managementsystem.user.model.User;
import com.railway.managementsystem.user.repository.UserMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TenantFilterInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;
    private final UserMapper UserMapper; // To fetch user details

    @Override
    @Transactional // Important: to have a session available
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only apply filter if the user is authenticated
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String currentUsername = authentication.getName();

            // We need to fetch the user to get their department
            UserMapper.findByUsername(currentUsername).ifPresent(user -> {
                if (user.getDepartment() != null) {
                    Long tenantId = user.getDepartment().getId();
                    Session session = entityManager.unwrap(Session.class);
                    session.enableFilter("tenantFilter").setParameter("departmentId", tenantId);
                }
                // If the user has no department (e.g., a system-wide super-admin),
                // we don't enable the filter, giving them access to all data.
            });
        }
        return true; // Continue the request
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // It's good practice to disable the filter after the request is complete,
        // though with session-per-request, it's less critical.
        Session session = entityManager.unwrap(Session.class);
        if (session != null && session.isOpen()) {
            session.disableFilter("tenantFilter");
        }
    }
}