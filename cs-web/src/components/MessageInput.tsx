import { FormEvent, useState } from "react";

interface MessageInputProps {
  disabled?: boolean;
  onSend: (message: string) => Promise<void>;
}

export function MessageInput({ disabled, onSend }: MessageInputProps) {
  const [value, setValue] = useState("");

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!value.trim() || disabled) {
      return;
    }
    const toSend = value;
    setValue("");
    await onSend(toSend);
  }

  return (
    <form className="message-input-form" onSubmit={handleSubmit}>
      <input
        value={value}
        onChange={(event) => setValue(event.target.value)}
        className="message-input"
        placeholder="请输入你的问题，例如：退款流程是什么？"
        disabled={disabled}
      />
      <button type="submit" disabled={disabled || !value.trim()}>
        发送
      </button>
    </form>
  );
}
