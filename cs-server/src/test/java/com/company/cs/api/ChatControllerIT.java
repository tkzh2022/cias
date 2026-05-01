package com.company.cs.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.cs.api.dto.ChatResponse;
import com.company.cs.api.dto.KnowledgeSource;
import com.company.cs.infra.monitoring.AlarmNotifier;
import com.company.cs.service.ChatService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ChatController.class)
class ChatControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private AlarmNotifier alarmNotifier;

    @Test
    void shouldReturnChatResponse() throws Exception {
        ChatResponse mock = new ChatResponse(
                "req-1",
                "s1",
                "您好，这里是智能客服。",
                "FAST",
                false,
                List.of(new KnowledgeSource("k1", "退款政策", "7天无理由退款", 0.9))
        );
        when(chatService.chat(org.mockito.ArgumentMatchers.any())).thenReturn(mock);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId":"s1",
                                  "userId":"u1",
                                  "message":"你好",
                                  "channel":"web"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-1"))
                .andExpect(jsonPath("$.route").value("FAST"));
    }
}
