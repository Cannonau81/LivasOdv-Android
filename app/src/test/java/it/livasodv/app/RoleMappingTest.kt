package it.livasodv.app

import it.livasodv.app.data.AppRole
import it.livasodv.app.feature.AccessArea
import it.livasodv.app.feature.roleAllowsArea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleMappingTest {
    @Test fun serverRolesMapCorrectly() {
        assertEquals(AppRole.DIRETTIVO, AppRole.fromServer("admin"))
        assertEquals(AppRole.DIRETTIVO, AppRole.fromServer("direttivo"))
        assertEquals(AppRole.MAGAZZINO, AppRole.fromServer("magazzino"))
        assertEquals(AppRole.SERVIZI_SOCIALI, AppRole.fromServer("social_services"))
        assertEquals(AppRole.SOCIO, AppRole.fromServer("unknown"))
    }

    @Test fun protectedAreasRespectRoles() {
        assertTrue(roleAllowsArea(AppRole.DIRETTIVO, AccessArea.DIRETTIVO))
        assertTrue(roleAllowsArea(AppRole.MAGAZZINO, AccessArea.MAGAZZINO))
        assertTrue(roleAllowsArea(AppRole.OLP, AccessArea.SERVIZIO_CIVILE))
        assertFalse(roleAllowsArea(AppRole.SOCIO, AccessArea.DIRETTIVO))
        assertFalse(roleAllowsArea(AppRole.SOCIO, AccessArea.MAGAZZINO))
    }
}
