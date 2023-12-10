package org.example.controllers;

import org.example.data.H2User;
import org.example.service.FileExportService;
import org.example.sql.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.SmartValidator;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SQLControllerTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private SmartValidator validator;
    @Mock
    private FileExportService exportService;
    @Mock
    private HttpServletRequest request;

    private SQLController sqlController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sqlController = new SQLController(userRepo, validator, exportService);
    }

    @Test
    public void testDBKafkaUsers() {
        when(userRepo.count()).thenReturn(2L);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(validator.validate(any(H2User.class), any())).thenReturn(true);
        when(userRepo.save(any(H2User.class))).thenReturn(new H2User("test", "test@local.host"));

        String response = sqlController.testDBKafkaUsers(request);

        assertEquals("DB Size=2->2 auto-rejected=0", response);
        verify(userRepo, times(2)).save(any(H2User.class));
        verify(exportService, times(1)).export(anyString(), any());
    }
}
