import type { MessageItem } from "../types";

interface ChatWindowProps {
  messages: MessageItem[];
}

export function ChatWindow({ messages }: ChatWindowProps) {
  return (
    <div className="chat-window">
      {messages.map((message) => (
        <div key={message.id} className={`message-row ${message.role}`}>
          <div className="message-bubble">
            {message.content}
            {message.loading ? <span className="typing-dot">...</span> : null}
          </div>
        </div>
      ))}
    </div>
  );
}
