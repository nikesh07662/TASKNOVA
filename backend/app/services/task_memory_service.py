import json
from typing import List, Dict, Any, Optional

class TaskMemoryService:
    """
    Universal Task Memory service.
    Persistent academic context linking:
    - Assignments
    - Documents
    - AI conversations
    - Imported references (ChatGPT / Gemini exports)
    - Cloud files
    - Deadlines
    - Submission history
    - Notifications
    """

    def __init__(self):
        # In-memory index for fast lookup, synced to DB in production
        self.memory_store: Dict[str, List[Dict[str, Any]]] = {}

    def store_memory(
        self,
        user_id: str,
        title: str,
        content: str,
        category: str,
        related_entity_id: Optional[str] = None,
        metadata: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        item = {
            "id": f"mem_{category}_{len(self.memory_store.get(user_id, [])) + 1}",
            "title": title,
            "content": content,
            "category": category,
            "related_entity_id": related_entity_id,
            "metadata": metadata or {}
        }
        if user_id not in self.memory_store:
            self.memory_store[user_id] = []
        self.memory_store[user_id].append(item)
        return item

    def search_memory(self, user_id: str, query: str, limit: int = 5) -> List[Dict[str, Any]]:
        user_items = self.memory_store.get(user_id, [])
        query_words = set(query.lower().split())
        scored_items = []

        for item in user_items:
            content_words = set((item["title"] + " " + item["content"]).lower().split())
            score = len(query_words.intersection(content_words))
            scored_items.append((score, item))

        scored_items.sort(key=lambda x: x[0], reverse=True)
        return [item for _, item in scored_items[:limit]]

    def import_chat_export(self, user_id: str, provider: str, raw_data: Dict[str, Any]) -> int:
        """Parses imported ChatGPT or Gemini conversation export."""
        count = 0
        conversations = raw_data if isinstance(raw_data, list) else raw_data.get("conversations", [raw_data])
        for conv in conversations:
            title = conv.get("title", f"{provider} Imported Conversation")
            text_snippets = []
            mapping = conv.get("mapping", {})
            for node_id, node in mapping.items():
                msg = node.get("message")
                if msg and "content" in msg:
                    parts = msg["content"].get("parts", [])
                    for part in parts:
                        if isinstance(part, str):
                            text_snippets.append(part)

            joined_content = "\n".join(text_snippets[:10]) or str(conv)[:500]
            self.store_memory(
                user_id=user_id,
                title=title,
                content=joined_content,
                category="CONVERSATION",
                metadata={"imported_from": provider}
            )
            count += 1
        return count

task_memory_service = TaskMemoryService()
