package org.newsanalyzer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.dto.CreateEntityRequest;
import org.newsanalyzer.dto.EntityDTO;
import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.EntityType;
import org.newsanalyzer.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for EntityController using MockMvc.
 * Tests REST API endpoints with mocked service layer.
 */
@WebMvcTest(EntityController.class)
class EntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntityService entityService;

    private CreateEntityRequest createRequest;
    private EntityDTO entityDTO;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        // Setup test request
        createRequest = new CreateEntityRequest();
        createRequest.setEntityType(EntityType.PERSON);
        createRequest.setName("Elizabeth Warren");

        Map<String, Object> properties = new HashMap<>();
        properties.put("jobTitle", "Senator");
        createRequest.setProperties(properties);

        // Setup test DTO
        entityDTO = new EntityDTO();
        entityDTO.setId(testId);
        entityDTO.setEntityType(EntityType.PERSON);
        entityDTO.setName("Elizabeth Warren");
        entityDTO.setProperties(properties);
        entityDTO.setSchemaOrgType("Person");

        Map<String, Object> schemaOrgData = new HashMap<>();
        schemaOrgData.put("@type", "Person");
        schemaOrgData.put("name", "Elizabeth Warren");
        entityDTO.setSchemaOrgData(schemaOrgData);

        entityDTO.setConfidenceScore(1.0f);
        entityDTO.setVerified(false);
        entityDTO.setCreatedAt(LocalDateTime.now());
        entityDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void testCreateEntity() throws Exception {
        when(entityService.createEntity(any(CreateEntityRequest.class))).thenReturn(entityDTO);

        mockMvc.perform(post("/api/entities")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(testId.toString()))
            .andExpect(jsonPath("$.name").value("Elizabeth Warren"))
            .andExpect(jsonPath("$.entityType").value("PERSON"))
            .andExpect(jsonPath("$.schemaOrgType").value("Person"));

        verify(entityService).createEntity(any(CreateEntityRequest.class));
    }

    @Test
    @WithMockUser
    void testGetAllEntities() throws Exception {
        List<EntityDTO> entities = Arrays.asList(entityDTO);
        when(entityService.getAllEntities()).thenReturn(entities);

        mockMvc.perform(get("/api/entities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(testId.toString()))
            .andExpect(jsonPath("$[0].name").value("Elizabeth Warren"));

        verify(entityService).getAllEntities();
    }

    @Test
    @WithMockUser
    void testGetEntityById() throws Exception {
        when(entityService.getEntityById(testId)).thenReturn(entityDTO);

        mockMvc.perform(get("/api/entities/{id}", testId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testId.toString()))
            .andExpect(jsonPath("$.name").value("Elizabeth Warren"));

        verify(entityService).getEntityById(testId);
    }

    @Test
    @WithMockUser
    void testGetEntityByIdNotFound() throws Exception {
        when(entityService.getEntityById(testId)).thenThrow(new ResourceNotFoundException("Entity", testId));

        mockMvc.perform(get("/api/entities/{id}", testId))
            .andExpect(status().isNotFound());

        verify(entityService).getEntityById(testId);
    }

    @Test
    @WithMockUser
    void testUpdateEntity() throws Exception {
        EntityDTO updatedDTO = new EntityDTO();
        updatedDTO.setId(testId);
        updatedDTO.setName("Elizabeth Warren Updated");
        updatedDTO.setEntityType(EntityType.PERSON);
        updatedDTO.setSchemaOrgType("Person");

        when(entityService.updateEntity(eq(testId), any(CreateEntityRequest.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/entities/{id}", testId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testId.toString()))
            .andExpect(jsonPath("$.name").value("Elizabeth Warren Updated"));

        verify(entityService).updateEntity(eq(testId), any(CreateEntityRequest.class));
    }

    @Test
    @WithMockUser
    void testDeleteEntity() throws Exception {
        doNothing().when(entityService).deleteEntity(testId);

        mockMvc.perform(delete("/api/entities/{id}", testId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(entityService).deleteEntity(testId);
    }

    @Test
    @WithMockUser
    void testGetEntitiesByType() throws Exception {
        List<EntityDTO> entities = Arrays.asList(entityDTO);
        when(entityService.getEntitiesByType(EntityType.PERSON)).thenReturn(entities);

        mockMvc.perform(get("/api/entities/type/{type}", EntityType.PERSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].entityType").value("PERSON"));

        verify(entityService).getEntitiesByType(EntityType.PERSON);
    }

    @Test
    @WithMockUser
    void testGetEntitiesBySchemaOrgType() throws Exception {
        List<EntityDTO> entities = Arrays.asList(entityDTO);
        when(entityService.getEntitiesBySchemaOrgType("Person")).thenReturn(entities);

        mockMvc.perform(get("/api/entities/schema-org-type/{schemaOrgType}", "Person"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].schemaOrgType").value("Person"));

        verify(entityService).getEntitiesBySchemaOrgType("Person");
    }

    @Test
    @WithMockUser
    void testSearchEntities() throws Exception {
        List<EntityDTO> entities = Arrays.asList(entityDTO);
        when(entityService.searchEntitiesByName("warren")).thenReturn(entities);

        mockMvc.perform(get("/api/entities/search")
                .param("q", "warren"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Elizabeth Warren"));

        verify(entityService).searchEntitiesByName("warren");
    }

    @Test
    @WithMockUser
    void testFullTextSearch() throws Exception {
        List<EntityDTO> entities = Arrays.asList(entityDTO);
        when(entityService.fullTextSearch("senator", 10)).thenReturn(entities);

        mockMvc.perform(get("/api/entities/search/fulltext")
                .param("q", "senator")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Elizabeth Warren"));

        verify(entityService).fullTextSearch("senator", 10);
    }

    @Test
    @WithMockUser
    void testVerifyEntity() throws Exception {
        EntityDTO verifiedDTO = new EntityDTO();
        verifiedDTO.setId(testId);
        verifiedDTO.setName("Elizabeth Warren");
        verifiedDTO.setVerified(true);

        when(entityService.verifyEntity(testId)).thenReturn(verifiedDTO);

        mockMvc.perform(post("/api/entities/{id}/verify", testId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verified").value(true));

        verify(entityService).verifyEntity(testId);
    }

    @Test
    @WithMockUser
    void testGetRecentEntities() throws Exception {
        List<EntityDTO> entities = Arrays.asList(entityDTO);
        when(entityService.getRecentEntities(7)).thenReturn(entities);

        mockMvc.perform(get("/api/entities/recent")
                .param("days", "7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Elizabeth Warren"));

        verify(entityService).getRecentEntities(7);
    }

    @Test
    @WithMockUser
    void testCreateEntityWithInvalidData() throws Exception {
        CreateEntityRequest invalidRequest = new CreateEntityRequest();
        // Missing required fields

        mockMvc.perform(post("/api/entities")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(entityService, never()).createEntity(any());
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/entities"))
            .andExpect(status().isUnauthorized());

        verify(entityService, never()).getAllEntities();
    }

    @Test
    @WithMockUser
    void testCreateEntityReturnsSchemaOrgData() throws Exception {
        when(entityService.createEntity(any(CreateEntityRequest.class))).thenReturn(entityDTO);

        mockMvc.perform(post("/api/entities")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.schemaOrgData").exists())
            .andExpect(jsonPath("$.schemaOrgData.@type").value("Person"))
            .andExpect(jsonPath("$.schemaOrgData.name").value("Elizabeth Warren"));

        verify(entityService).createEntity(any(CreateEntityRequest.class));
    }
}
