package gr.atc.modapto.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

/*
 * Utility class to parse the JWT received token and extract user roles
 */
public class JwtUtils {
    
    @Value("${keycloak.client}")
    private static String client;
    
    // Claims
    private static final String CLAIM_REALM_ACCESS = "realm_access";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    private static final String ID_FIELD = "sub";
    private static final String PILOT_CODE = "pilot_code";
    private static final String USER_ROLE = "user_role";
    private static final String PILOT_ROLE = "pilot_role";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JwtUtils() {}

    public static List<String> extractUserRoles(Jwt jwt) {
        if (jwt == null) {
            return Collections.emptyList();
        }

        Map<String, Object> realmAccess = jwt.getClaimAsMap(CLAIM_REALM_ACCESS);
        if (realmAccess == null || !realmAccess.containsKey(CLAIM_ROLES)) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get(CLAIM_ROLES);
        if (rolesObj instanceof List<?> rolesList) {
            return rolesList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        return Collections.emptyList();
    }

    /**
     * Util to retrieve pilot code from JWT Token
     *
     * @param jwt Token to extract userId
     * @return userId
     */
    public static String extractPilotCode(Jwt jwt){
        if (jwt == null || jwt.getClaimAsString(PILOT_CODE) == null) {
            return null;
        }
        return jwt.getClaimAsStringList(PILOT_CODE).getFirst();
    }

    /**
     * Util to retrieve pilot role from JWT Token
     *
     * @param jwt Token to extract pilot type
     * @return Pilot Type
     */
    public static String extractPilotRole(Jwt jwt){
        if (jwt == null || jwt.getClaimAsString(PILOT_ROLE) == null) {
            return null;
        }
        return jwt.getClaimAsStringList(PILOT_ROLE).getFirst();
    }

    /**
     * Util to extract the userId from Token
     *
     * @param jwt Token to extract userId
     * @return userId
     */
    public static String extractUserId(Jwt jwt){
        if (jwt == null || jwt.getClaimAsString(ID_FIELD) == null) {
            return null;
        }
        return jwt.getClaimAsString(ID_FIELD);
    }

    /**
     * Util to retrieve user role of user from JWT Token
     *
     * @param jwt Token to extract user role
     * @return user role
     */
    public static String extractUserRole(Jwt jwt){
        if (jwt == null || jwt.getClaimAsString(USER_ROLE) == null) {
            return null;
        }
        return jwt.getClaimAsStringList(USER_ROLE).getFirst();
    }

    /**
     * Util to extract User Type from JWT Token
     *
     * @param jwt Token to extract userId
     * @return List<String> for the User Roles
     */
    public static List<String> extractUserType(Jwt jwt){
        if (jwt == null) {
            return Collections.emptyList();
        }

        Map<String, Object> resourceAccess = jwt.getClaimAsMap(CLAIM_RESOURCE_ACCESS);
        if (resourceAccess == null || !resourceAccess.containsKey(client)) {
            return Collections.emptyList();
        }

        JsonNode roles = objectMapper.valueToTree(resourceAccess.get(client));
        if (roles == null || !roles.has(CLAIM_ROLES))
            return Collections.emptyList();

        Object pilotRolesObj = roles.get(CLAIM_ROLES);

        if (pilotRolesObj instanceof ArrayNode pilotRolesArray) {
            // Convert ArrayNode to List<String>
            List<String> pilotRolesList = new ArrayList<>();
            for (JsonNode roleNode : pilotRolesArray) {
                pilotRolesList.add(roleNode.asText());
            }

            return pilotRolesList;
        }

        return Collections.emptyList();
    }
}