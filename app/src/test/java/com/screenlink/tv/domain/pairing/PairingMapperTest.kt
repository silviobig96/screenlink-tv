package com.screenlink.tv.domain.pairing

import com.screenlink.tv.core.network.dto.PairingStatusDto
import com.screenlink.tv.data.pairing.mappers.toDomain
import com.screenlink.tv.domain.pairing.models.PairingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PairingMapperTest {
    @Test
    fun `pending maps to pending`() {
        assertTrue(PairingStatusDto("pending").toDomain() is PairingStatus.Pending)
    }

    @Test
    fun `paired maps token`() {
        assertEquals("token", (PairingStatusDto("paired", "token").toDomain() as PairingStatus.Paired).deviceToken)
    }

    @Test
    fun `paired without token remains explicit`() {
        assertNull((PairingStatusDto("paired").toDomain() as PairingStatus.Paired).deviceToken)
    }
}
