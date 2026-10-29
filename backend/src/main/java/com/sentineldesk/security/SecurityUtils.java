package com.sentineldesk.security;

import com.sentineldesk.user.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static boolean hasRole(Role role) {
        UserPrincipal user = getCurrentUser();
        return user != null && user.getRole() == role;
    }

    public static boolean isStaff() {
        UserPrincipal user = getCurrentUser();
        if (user == null) return false;
        return user.getRole() == Role.ADMIN ||
               user.getRole() == Role.SUPPORT_MANAGER ||
               user.getRole() == Role.SUPPORT_AGENT;
    }

    public static boolean isRequester() {
        return hasRole(Role.REQUESTER);
    }

    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public static boolean isManager() {
        return hasRole(Role.SUPPORT_MANAGER) || hasRole(Role.ADMIN);
    }
}
