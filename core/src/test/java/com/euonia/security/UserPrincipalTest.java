package com.euonia.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserPrincipal")
class UserPrincipalTest {

    // region 基本主体查询测试

    @Test
    @DisplayName("Given subject with principals when querying principal info then name authentication and roles are resolved")
    void givenSubjectWithPrincipalsWhenQueryingThenNameAuthAndRolesResolve() {
        Subject subject = new Subject();
        subject.getPrincipals().add((Principal) () -> "alice");
        subject.getPrincipals().add((Principal) () -> "admin");

        UserPrincipal userPrincipal = new UserPrincipal(subject);

        assertSame(subject, userPrincipal.getSubject());
        assertEquals("alice", userPrincipal.getName());
        assertTrue(userPrincipal.isAuthenticated());
        assertTrue(userPrincipal.hasRole("admin"));
        assertTrue(userPrincipal.isInRole("admin"));
        assertTrue(userPrincipal.isInRoles("guest", "admin"));
        assertNull(userPrincipal.getClaim(UserClaimTypes.EMAIL));
    }

    @Test
    @DisplayName("Given null or empty subject when querying then unauthenticated behavior is returned")
    void givenNullOrEmptySubjectWhenQueryingThenUnauthenticatedBehaviorReturned() {
        UserPrincipal withNullSubject = new UserPrincipal(null);
        UserPrincipal withEmptySubject = new UserPrincipal(new Subject());

        // null subject
        assertFalse(withNullSubject.isAuthenticated());
        assertFalse(withNullSubject.hasRole("admin"));
        assertFalse(withNullSubject.isInRole("admin"));
        assertFalse(withNullSubject.isInRoles("admin"));
        assertNull(withNullSubject.getName());
        assertNull(withNullSubject.getUserId());
        assertNull(withNullSubject.getUsername());

        // empty subject
        assertFalse(withEmptySubject.isAuthenticated());
        assertFalse(withEmptySubject.hasRole("admin"));
        assertFalse(withEmptySubject.isInRole("admin"));
        assertFalse(withEmptySubject.isInRoles("admin"));
        assertNull(withEmptySubject.getName());
    }

    // endregion

    // region 类型化声明访问器测试

    @Test
    @DisplayName("Given subject with claim principals when accessing typed properties then values are resolved")
    void givenSubjectWithClaimPrincipalsWhenAccessingTypedPropertiesThenValuesResolve() {
        Subject subject = new Subject();
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.SUBJECT, "user-123"));
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.NAME, "John Doe"));
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.CODE, "JD001"));
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.TENANT, "tenant-abc"));
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.ROLE, "admin"));
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.ROLE, "user"));
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.EMAIL, "john@example.com"));

        UserPrincipal userPrincipal = new UserPrincipal(subject);

        assertEquals("user-123", userPrincipal.getUserId());
        assertEquals("John Doe", userPrincipal.getUsername());
        assertEquals("JD001", userPrincipal.getCode());
        assertEquals("tenant-abc", userPrincipal.getTenant());
        assertEquals("john@example.com", userPrincipal.getClaim(UserClaimTypes.EMAIL));

        List<String> roles = userPrincipal.getRoles();
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("admin"));
        assertTrue(roles.contains("user"));
    }

    @Test
    @DisplayName("Given subject without claim principals when accessing typed properties then null or empty are returned")
    void givenSubjectWithoutClaimPrincipalsWhenAccessingTypedPropertiesThenNullReturned() {
        Subject subject = new Subject();
        subject.getPrincipals().add((Principal) () -> "plain-principal");

        UserPrincipal userPrincipal = new UserPrincipal(subject);

        assertNull(userPrincipal.getUserId());
        assertNull(userPrincipal.getUsername());
        assertNull(userPrincipal.getCode());
        assertNull(userPrincipal.getTenant());
        assertTrue(userPrincipal.getRoles().isEmpty());
    }

    // endregion

    // region 声明查询方法测试

    @Test
    @DisplayName("Given subject with claims when finding claims then matching claims are returned")
    void givenSubjectWithClaimsWhenFindingThenMatchingClaimsReturned() {
        Subject subject = new Subject();
        UserClaim emailClaim = new UserClaim(UserClaimTypes.EMAIL, "user@example.com");
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.SUBJECT, "sub-001"));
        subject.getPrincipals().add(emailClaim);
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.ROLE, "admin"));

        UserPrincipal userPrincipal = new UserPrincipal(subject);

        // findClaim
        UserClaim found = userPrincipal.findClaim(UserClaimTypes.EMAIL);
        assertNotNull(found);
        assertEquals(UserClaimTypes.EMAIL, found.getType());
        assertEquals("user@example.com", found.getValue());

        // findClaim - non-existent
        assertNull(userPrincipal.findClaim("non-existent"));

        // findClaims
        List<UserClaim> roleClaims = userPrincipal.findClaims(UserClaimTypes.ROLE);
        assertEquals(1, roleClaims.size());
        assertEquals("admin", roleClaims.get(0).getValue());

        // getAllClaims
        List<UserClaim> allClaims = userPrincipal.getAllClaims();
        assertEquals(3, allClaims.size());
    }

    @Test
    @DisplayName("Given null subject when querying claims then null or empty are returned")
    void givenNullSubjectWhenQueryingClaimsThenNullOrEmptyReturned() {
        UserPrincipal userPrincipal = new UserPrincipal(null);

        assertNull(userPrincipal.findClaim(UserClaimTypes.EMAIL));
        assertTrue(userPrincipal.findClaims(UserClaimTypes.EMAIL).isEmpty());
        assertTrue(userPrincipal.getAllClaims().isEmpty());
        assertNull(userPrincipal.getClaim(UserClaimTypes.EMAIL));
    }

    // endregion

    // region 角色检查测试

    @Test
    @DisplayName("Given authenticated subject when checking roles then isInRole and isInRoles work correctly")
    void givenAuthenticatedSubjectWhenCheckingRolesThenCorrect() {
        Subject subject = new Subject();
        subject.getPrincipals().add((Principal) () -> "admin");
        subject.getPrincipals().add((Principal) () -> "user");
        UserPrincipal userPrincipal = new UserPrincipal(subject);

        // isInRole (new name)
        assertTrue(userPrincipal.isInRole("admin"));
        assertTrue(userPrincipal.isInRole("user"));
        assertFalse(userPrincipal.isInRole("superadmin"));

        // hasRole (deprecated backward compat)
        assertTrue(userPrincipal.hasRole("admin"));

        // isInRoles varargs
        assertTrue(userPrincipal.isInRoles("superadmin", "admin"));
        assertTrue(userPrincipal.isInRoles("admin", "user"));
        assertFalse(userPrincipal.isInRoles("superadmin", "root"));

        // isInRoles edge cases
        assertFalse(userPrincipal.isInRoles());
        assertFalse(userPrincipal.isInRoles((String[]) null));
    }

    @Test
    @DisplayName("Given authenticated subject when using isInRoles with separator then roles are parsed and checked")
    void givenAuthenticatedSubjectWhenUsingIsInRolesWithSeparatorThenRolesParsed() {
        Subject subject = new Subject();
        subject.getPrincipals().add((Principal) () -> "admin");
        subject.getPrincipals().add((Principal) () -> "user");
        UserPrincipal userPrincipal = new UserPrincipal(subject);

        // Separator ','
        assertTrue(userPrincipal.isInRoles("admin,user,superadmin", ','));
        assertFalse(userPrincipal.isInRoles("superadmin,root", ','));

        // Custom separator ';'
        assertTrue(userPrincipal.isInRoles("admin;user", ';'));
        assertFalse(userPrincipal.isInRoles("superadmin;root", ';'));

        // Whitespace handling - roles include whitespace, so exact match required
        Subject subject2 = new Subject();
        subject2.getPrincipals().add((Principal) () -> "admin");
        UserPrincipal up2 = new UserPrincipal(subject2);
        assertFalse(up2.isInRoles(" admin , user ", ','));
    }

    // endregion

    // region 断言方法测试

    @Test
    @DisplayName("Given unauthenticated or unauthorized user when ensuring access then security exceptions are thrown")
    void givenUnauthenticatedOrUnauthorizedUserWhenEnsuringAccessThenThrow() {
        UserPrincipal unauthenticated = new UserPrincipal(new Subject());

        AuthenticationException authException = assertThrows(AuthenticationException.class, unauthenticated::ensureAuthenticated);
        assertEquals("User is not authenticated", authException.getMessage());

        Subject subject = new Subject();
        subject.getPrincipals().add((Principal) () -> "user");
        UserPrincipal authenticated = new UserPrincipal(subject);

        UnauthorizedAccessException roleException = assertThrows(UnauthorizedAccessException.class,
            () -> authenticated.ensureHasRole("admin"));
        assertEquals("User does not have required role: admin", roleException.getMessage());

        UnauthorizedAccessException rolesException = assertThrows(UnauthorizedAccessException.class,
            () -> authenticated.ensureInRoles("admin", "ops"));
        assertEquals("User does not have any of the required roles: admin, ops", rolesException.getMessage());
    }

    @Test
    @DisplayName("Given authorized user when ensuring role membership then no exception is thrown")
    void givenAuthorizedUserWhenEnsuringRoleMembershipThenNoExceptionThrown() {
        Subject subject = new Subject();
        subject.getPrincipals().add((Principal) () -> "dev");
        UserPrincipal userPrincipal = new UserPrincipal(subject);

        assertDoesNotThrow(userPrincipal::ensureAuthenticated);
        assertDoesNotThrow(() -> userPrincipal.ensureHasRole("dev"));
        assertDoesNotThrow(() -> userPrincipal.ensureInRoles("ops", "dev"));
    }

    @Test
    @DisplayName("Given subject without required claim when ensuring claim then unauthorized exception is thrown")
    void givenSubjectWithoutClaimWhenEnsuringClaimThenThrow() {
        Subject subject = new Subject();
        subject.getPrincipals().add(new UserClaim(UserClaimTypes.EMAIL, "user@example.com"));
        UserPrincipal userPrincipal = new UserPrincipal(subject);

        assertDoesNotThrow(() -> userPrincipal.ensureHasClaim(UserClaimTypes.EMAIL));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
            () -> userPrincipal.ensureHasClaim(UserClaimTypes.SUBJECT));
        assertTrue(exception.getMessage().contains(UserClaimTypes.SUBJECT));
    }

    // endregion

    // region UserClaim 单元测试

    @Test
    @DisplayName("Given valid type and value when constructing UserClaim then properties are set")
    void givenValidTypeAndValueWhenConstructingUserClaimThenPropertiesSet() {
        UserClaim claim = new UserClaim("type1", "value1");

        assertEquals("type1", claim.getType());
        assertEquals("value1", claim.getValue());
        assertEquals("type1", claim.getName());
        assertEquals("type1: value1", claim.toString());
    }

    @Test
    @DisplayName("Given two identical UserClaims when checking equality then they are equal")
    void givenTwoIdenticalUserClaimsWhenCheckingEqualityThenEqual() {
        UserClaim claim1 = new UserClaim("type", "value");
        UserClaim claim2 = new UserClaim("type", "value");
        UserClaim claim3 = new UserClaim("type", "different");

        assertEquals(claim1, claim2);
        assertEquals(claim1.hashCode(), claim2.hashCode());
        assertNotEquals(claim1, claim3);
        assertNotEquals(claim1, null);
        assertNotEquals(claim1, "not a claim");
    }

    @Test
    @DisplayName("Given null type when constructing UserClaim then NullPointerException is thrown")
    void givenNullTypeWhenConstructingUserClaimThenThrow() {
        assertThrows(NullPointerException.class, () -> new UserClaim(null, "value"));
    }

    @Test
    @DisplayName("Given null value when constructing UserClaim then getValue returns null")
    void givenNullValueWhenConstructingUserClaimThenGetValueReturnsNull() {
        UserClaim claim = new UserClaim("type", null);
        assertNull(claim.getValue());
    }

    // endregion
}
