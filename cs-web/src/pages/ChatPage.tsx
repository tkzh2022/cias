import { useMemo, useState } from "react";
import { ChatWindow } from "../components/ChatWindow";
import { KnowledgeSourcePanel } from "../components/KnowledgeSourcePanel";
import { MessageInput } from "../components/MessageInput";
import type { ChatApiResponse, MessageItem, SourceItem } from "../types";

function uniqueId(prefix: string): string {
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export function ChatPage() {
  const [messages, setMessages] = useState<MessageItem[]>([
    {
      id: uniqueId("welcome"),
      role: "assistant",
      content: "你好，我是智能客服助手。请输入你的问题。",
    },
  ]);
  const [sources, setSources] = useState<SourceItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [lastQuestion, setLastQuestion] = useState("");
  const sessionId = useMemo(() => uniqueId("session"), []);

  async function sendMessage(message: string) {
    setLastQuestion(message);
    const userMessage: MessageItem = { id: uniqueId("user"), role: "user", content: message };
    const assistantId = uniqueId("assistant");
    setMessages((old) => [
      ...old,
      userMessage,
      { id: assistantId, role: "assistant", content: "正在思考中", loading: true },
    ]);
    setLoading(true);

    try {
      const response = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          sessionId,
          userId: "demo-user",
          message,
          channel: "web",
        }),
      });
      if (!response.ok) {
        throw new Error(`request failed with status ${response.status}`);
      }
      const data: ChatApiResponse = await response.json();
      const streamText = await fakeStream(data.answer);
      setMessages((old) =>
        old.map((item) =>
          item.id === assistantId ? { ...item, content: streamText, loading: false } : item
        )
      );
      setSources(data.sources ?? []);
    } catch (error) {
      setMessages((old) =>
        old.map((item) =>
          item.id === assistantId
            ? { ...item, loading: false, content: "请求失败，请稍后重试或联系人工客服。" }
            : item
        )
      );
    } finally {
      setLoading(false);
    }
  }

  async function fakeStream(text: string): Promise<string> {
    let current = "";
    for (const ch of text) {
      current += ch;
      await new Promise((resolve) => setTimeout(resolve, 8));
    }
    return current;
  }

  return (
    <main className="chat-page">
      <section className="chat-main">
        <header className="chat-header">
          <h1>智能客服系统</h1>
          <button
            type="button"
            disabled={loading || !lastQuestion}
            onClick={() => sendMessage(lastQuestion)}
          >
            失败重试
          </button>
        </header>
        <ChatWindow messages={messages} />
        <MessageInput disabled={loading} onSend={sendMessage} />
      </section>
      <KnowledgeSourcePanel sources={sources} />
    </main>
  );
}
