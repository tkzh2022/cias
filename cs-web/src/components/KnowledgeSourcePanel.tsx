import type { SourceItem } from "../types";

interface KnowledgeSourcePanelProps {
  sources: SourceItem[];
}

export function KnowledgeSourcePanel({ sources }: KnowledgeSourcePanelProps) {
  return (
    <aside className="source-panel">
      <h3>知识来源</h3>
      {sources.length === 0 ? (
        <p className="empty-tip">暂无引用来源</p>
      ) : (
        <ul>
          {sources.map((source) => (
            <li key={source.id}>
              <div className="source-title">{source.title}</div>
              <div className="source-snippet">{source.snippet}</div>
              <div className="source-score">相关度: {source.score.toFixed(2)}</div>
            </li>
          ))}
        </ul>
      )}
    </aside>
  );
}
