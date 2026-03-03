/**
 * React前端消费Spring Boot流式响应的示例
 * 
 * 这个文件展示了如何在前端使用EventSource来接收流式响应
 */

// 使用React Hooks实现流式聊天组件
import React, { useState, useRef, useEffect } from 'react';

const ChatInterface = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sessionId, setSessionId] = useState('session-' + Date.now());
  const [useHistory, setUseHistory] = useState(false);
  const eventSourceRef = useRef(null);

  // 清理函数，组件卸载时关闭连接
  useEffect(() => {
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, []);

  // 处理流式聊天
  const handleStreamChat = async (message) => {
    if (!message.trim()) return;

    // 添加用户消息
    const userMessage = { id: Date.now(), type: 'user', content: message };
    setMessages(prev => [...prev, userMessage]);
    
    setIsLoading(true);
    
    // 关闭之前的连接
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    try {
      // 根据是否使用历史记录选择不同的API端点
      const endpoint = useHistory ? 'stream-chat-with-history' : 'stream-chat';
      const encodedMessage = encodeURIComponent(message);
      const params = `message=${encodedMessage}&sessionId=${sessionId}`;
      
      eventSourceRef.current = new EventSource(`http://localhost:7340/api/ai/${endpoint}?${params}`);

      // 初始化AI消息
      let aiResponse = '';
      const aiMessageId = Date.now();
      
      setMessages(prev => [...prev, { 
        id: aiMessageId, 
        type: 'ai', 
        content: '' 
      }]);

      // 处理接收到的事件
      eventSourceRef.current.onmessage = (event) => {
        const data = JSON.parse(event.data);
        
        switch (data.name) {
          case 'chunk':
            // 更新AI消息内容
            aiResponse += data.data;
            setMessages(prev => 
              prev.map(msg => 
                msg.id === aiMessageId 
                  ? { ...msg, content: msg.content + data.data } 
                  : msg
              )
            );
            break;
            
          case 'complete':
            // 完成时关闭连接
            eventSourceRef.current.close();
            setIsLoading(false);
            break;
            
          default:
            console.log('Unknown event:', data);
        }
      };

      // 错误处理
      eventSourceRef.current.onerror = (err) => {
        console.error('SSE error:', err);
        eventSourceRef.current.close();
        setIsLoading(false);
        
        // 添加错误消息
        setMessages(prev => [...prev, { 
          id: Date.now(), 
          type: 'error', 
          content: 'AI响应出现错误，请重试' 
        }]);
      };

    } catch (error) {
      console.error('Stream chat error:', error);
      setIsLoading(false);
    }
  };

  // 处理普通聊天（非流式）
  const handleSimpleChat = async (message) => {
    if (!message.trim()) return;

    // 添加用户消息
    const userMessage = { id: Date.now(), type: 'user', content: message };
    setMessages(prev => [...prev, userMessage]);
    
    setIsLoading(true);

    try {
      // 根据是否使用历史记录选择不同的API端点
      const endpoint = useHistory ? 'chat-with-history' : 'simple-chat';
      const encodedMessage = encodeURIComponent(message);
      const params = `message=${encodedMessage}&sessionId=${sessionId}`;
      
      const response = await fetch(`http://localhost:7340/api/ai/${endpoint}?${params}`);
      const aiResponse = await response.text();
      
      // 添加AI消息
      setMessages(prev => [...prev, { 
        id: Date.now(), 
        type: 'ai', 
        content: aiResponse 
      }]);
      
    } catch (error) {
      console.error('Simple chat error:', error);
      setMessages(prev => [...prev, { 
        id: Date.now(), 
        type: 'error', 
        content: 'AI响应出现错误，请重试' 
      }]);
    } finally {
      setIsLoading(false);
    }
  };

  // 处理React Agent请求
  const handleReactAgent = async (message) => {
    if (!message.trim()) return;

    const userMessage = { id: Date.now(), type: 'user', content: message };
    setMessages(prev => [...prev, userMessage]);
    
    setIsLoading(true);

    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    try {
      const encodedMessage = encodeURIComponent(message);
      eventSourceRef.current = new EventSource(`http://localhost:7340/api/ai/react-agent?message=${encodedMessage}`);

      let fullResponse = '';
      const aiMessageId = Date.now();
      
      setMessages(prev => [...prev, { 
        id: aiMessageId, 
        type: 'ai', 
        content: '' 
      }]);

      eventSourceRef.current.onmessage = (event) => {
        const data = JSON.parse(event.data);
        
        switch (data.name) {
          case 'thought':
          case 'action':
          case 'observation':
          case 'response':
            fullResponse += `\n[${data.name}] ${data.data}`;
            setMessages(prev => 
              prev.map(msg => 
                msg.id === aiMessageId 
                  ? { ...msg, content: fullResponse } 
                  : msg
              )
            );
            break;
            
          case 'complete':
            eventSourceRef.current.close();
            setIsLoading(false);
            break;
        }
      };

      eventSourceRef.current.onerror = (err) => {
        console.error('React Agent SSE error:', err);
        eventSourceRef.current.close();
        setIsLoading(false);
      };

    } catch (error) {
      console.error('React Agent error:', error);
      setIsLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (input.trim()) {
      handleStreamChat(input);
      setInput('');
    }
  };

  const handleSimpleSubmit = () => {
    if (input.trim()) {
      handleSimpleChat(input);
      setInput('');
    }
  };

  const handleReactAgentSubmit = () => {
    if (input.trim()) {
      handleReactAgent(input);
      setInput('');
    }
  };

  const handleClearSession = () => {
    fetch(`http://localhost:7340/api/ai/clear-session/${sessionId}`, {
      method: 'POST'
    }).then(() => {
      setMessages([]); // 清空本地消息
      setSessionId('session-' + Date.now()); // 创建新会话ID
    });
  };

  return (
    <div className="chat-container">
      <div className="controls">
        <div className="session-controls">
          <span>会话ID: {sessionId}</span>
          <button onClick={handleClearSession} className="clear-session-btn">清空会话</button>
        </div>
        <div className="history-toggle">
          <label>
            <input
              type="checkbox"
              checked={useHistory}
              onChange={(e) => setUseHistory(e.target.checked)}
            />
            使用对话历史
          </label>
        </div>
      </div>
      
      <div className="chat-messages">
        {messages.map((message) => (
          <div key={message.id} className={`message ${message.type}`}>
            <div className="message-content">{message.content}</div>
          </div>
        ))}
        
        {isLoading && (
          <div className="message ai">
            <div className="typing-indicator">AI正在思考中...</div>
          </div>
        )}
      </div>
      
    <div className="controls">
        <div className="session-controls">
          <span>会话ID: {sessionId}</span>
          <button onClick={handleClearSession} className="clear-session-btn">清空会话</button>
        </div>
        <div className="history-toggle">
          <label>
            <input
              type="checkbox"
              checked={useHistory}
              onChange={(e) => setUseHistory(e.target.checked)}
            />
            使用对话历史
          </label>
        </div>
        <div className="skill-controls">
          <select 
            value={selectedSkill} 
            onChange={(e) => setSelectedSkill(e.target.value)}
            className="skill-select"
          >
            <option value="">选择技能...</option>
            <option value="reviewContract">合同审核</option>
            <option value="explainClause">条款解释</option>
            <option value="generateContractTemplate">合同模板生成</option>
            <option value="compareContracts">合同对比</option>
            <option value="assessRisk">风险评估</option>
            <option value="optimizeClause">条款优化</option>
          </select>
          <button 
            type="button" 
            onClick={handleExecuteSkill}
            disabled={isLoading}
            className="skill-button"
          >
            执行技能
          </button>
        </div>
      </div>
      
      <form onSubmit={handleSubmit} className="chat-input-form">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="输入您的消息..."
          disabled={isLoading}
          className="chat-input"
        >
        <button 
          type="button" 
          onClick={handleSimpleSubmit}
          disabled={isLoading}
          className="simple-button"
        >
          普通聊天
        </button>
        <button 
          type="submit" 
          disabled={isLoading}
          className="send-button"
        >
          流式发送
        </button>
        <button 
          type="button" 
          onClick={handleReactAgentSubmit}
          disabled={isLoading}
          className="agent-button"
        >
          React Agent
        </button>
      </form>
    </div>
  );
};

export default ChatInterface;

/**
 * 传统的JavaScript实现（非React）
 */
class SimpleChatClient {
  constructor(baseUrl = 'http://localhost:7340') {
    this.baseUrl = baseUrl;
    this.eventSource = null;
    this.sessionId = 'session-' + Date.now();
  }

  // 简单聊天
  simpleChat(message) {
    return fetch(`${this.baseUrl}/api/ai/simple-chat?message=${encodeURIComponent(message)}`)
      .then(response => response.text());
  }

  // 流式聊天
  streamChat(message, onChunk, onComplete, onError) {
    // 关闭之前的连接
    if (this.eventSource) {
      this.eventSource.close();
    }

    const encodedMessage = encodeURIComponent(message);
    this.eventSource = new EventSource(`${this.baseUrl}/api/ai/stream-chat?message=${encodedMessage}`);

    this.eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      
      if (data.name === 'chunk') {
        onChunk(data.data);
      } else if (data.name === 'complete') {
        onComplete(data.data);
        this.eventSource.close();
      }
    };

    this.eventSource.onerror = (err) => {
      onError(err);
      this.eventSource.close();
    };
  }

  // 执行技能
  executeSkill(skillName, params) {
    return fetch(`${this.baseUrl}/api/ai/execute-skill`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ ...params })
    })
    .then(response => response.text());
  }

  // 获取技能列表
  getSkills() {
    return fetch(`${this.baseUrl}/api/ai/skills`)
      .then(response => response.json());
  }

  // 关闭连接
  close() {
    if (this.eventSource) {
      this.eventSource.close();
    }
  }
}

  // 简单聊天
  simpleChat(message, useHistory = false) {
    const endpoint = useHistory ? 'chat-with-history' : 'simple-chat';
    const params = useHistory ? `?message=${encodeURIComponent(message)}&sessionId=${this.sessionId}` : 
                              `?message=${encodeURIComponent(message)}`;
    return fetch(`${this.baseUrl}/api/ai/${endpoint}${params}`)
      .then(response => response.text());
  }

  // 流式聊天
  streamChat(message, onChunk, onComplete, onError, useHistory = false) {
    // 关闭之前的连接
    if (this.eventSource) {
      this.eventSource.close();
    }

    const endpoint = useHistory ? 'stream-chat-with-history' : 'stream-chat';
    const params = useHistory ? `?message=${encodeURIComponent(message)}&sessionId=${this.sessionId}` : 
                              `?message=${encodeURIComponent(message)}`;
    
    this.eventSource = new EventSource(`${this.baseUrl}/api/ai/${endpoint}${params}`);

    this.eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      
      if (data.name === 'chunk') {
        onChunk(data.data);
      } else if (data.name === 'complete') {
        onComplete(data.data);
        this.eventSource.close();
      }
    };

    this.eventSource.onerror = (err) => {
      onError(err);
      this.eventSource.close();
    };
  }

  // 清空会话
  clearSession() {
    return fetch(`${this.baseUrl}/api/ai/clear-session/${this.sessionId}`, {
      method: 'POST'
    });
  }

  // 设置会话ID
  setSessionId(sessionId) {
    this.sessionId = sessionId;
  }

  // 关闭连接
  close() {
    if (this.eventSource) {
      this.eventSource.close();
    }
  }
}