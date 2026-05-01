export type Role = "user" | "assistant" | "system";

export interface SourceItem {
  id: string;
  title: string;
  snippet: string;
  score: number;
}

export interface MessageItem {
  id: string;
  role: Role;
  content: string;
  loading?: boolean;
}

export interface ChatApiResponse {
  requestId: string;
  sessionId: string;
  answer: string;
  route: string;
  handoff: boolean;
  sources: SourceItem[];
}
