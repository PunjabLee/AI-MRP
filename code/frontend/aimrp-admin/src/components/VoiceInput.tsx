/**
 * 语音输入组件
 * 
 * 支持功能：
 * - 语音录制
 * - 语音识别（需配置第三方服务）
 * - 实时语音转文字
 * 
 * 使用方式：
 * <VoiceInput onResult={(text) => console.log(text)} />
 */

import { useState, useRef } from 'react';

interface VoiceInputProps {
  onResult: (text: string) => void;
  language?: string;
  placeholder?: string;
}

export default function VoiceInput({ 
  onResult, 
  language = 'zh-CN',
  placeholder = '点击麦克风开始说话...'
}: VoiceInputProps) {
  const [isRecording, setIsRecording] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [isSupported, setIsSupported] = useState(true);
  
  const recognitionRef = useRef<any>(null);

  // 检查浏览器支持
  useState(() => {
    const SpeechRecognition = (window as any).SpeechRecognition || 
                              (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      setIsSupported(false);
    }
  });

  const startRecording = () => {
    if (!isSupported) {
      alert('您的浏览器不支持语音识别功能，请使用Chrome或Edge浏览器');
      return;
    }

    const SpeechRecognition = (window as any).SpeechRecognition || 
                              (window as any).webkitSpeechRecognition;
    
    const recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = language;

    recognition.onstart = () => {
      setIsRecording(true);
      setTranscript('');
    };

    recognition.onresult = (event: any) => {
      let finalTranscript = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;
        if (event.results[i].isFinal) {
          finalTranscript += transcript;
        }
      }
      if (finalTranscript) {
        setTranscript(finalTranscript);
      }
    };

    recognition.onerror = (event: any) => {
      console.error('语音识别错误:', event.error);
      setIsRecording(false);
    };

    recognition.onend = () => {
      setIsRecording(false);
      if (transcript) {
        onResult(transcript);
      }
    };

    recognitionRef.current = recognition;
    recognition.start();
  };

  const stopRecording = () => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
      setIsRecording(false);
    }
  };

  const handleClick = () => {
    if (isRecording) {
      stopRecording();
    } else {
      startRecording();
    }
  };

  if (!isSupported) {
    return (
      <div className="text-gray-400 text-sm">
        语音输入暂不支持，请使用Chrome或Edge浏览器
      </div>
    );
  }

  return (
    <div className="flex items-center gap-2">
      <button
        type="button"
        onClick={handleClick}
        className={`w-10 h-10 rounded-full flex items-center justify-center transition-all ${
          isRecording 
            ? 'bg-red-500 text-white animate-pulse' 
            : 'bg-gray-200 text-gray-600 hover:bg-gray-300'
        }`}
        title={isRecording ? '点击停止' : '点击开始语音输入'}
      >
        {isRecording ? (
          <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <rect x="6" y="6" width="12" height="12" rx="2" />
          </svg>
        ) : (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                  d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
          </svg>
        )}
      </button>
      
      <span className="text-sm text-gray-500">
        {isRecording ? '正在录音...' : '点击说话'}
      </span>
      
      {transcript && (
        <span className="text-sm text-gray-700 ml-2">
          {transcript}
        </span>
      )}
    </div>
  );
}

// 语音输入Hook
export function useVoiceInput() {
  const [isRecording, setIsRecording] = useState(false);
  const [transcript, setTranscript] = useState('');
  const recognitionRef = useRef<any>(null);

  const start = (language = 'zh-CN') => {
    const SpeechRecognition = (window as any).SpeechRecognition || 
                              (window as any).webkitSpeechRecognition;
    
    if (!SpeechRecognition) {
      console.error('语音识别不支持');
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = language;

    recognition.onstart = () => setIsRecording(true);
    
    recognition.onresult = (event: any) => {
      let final = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        if (event.results[i].isFinal) {
          final += event.results[i][0].transcript;
        }
      }
      if (final) setTranscript(final);
    };

    recognition.onend = () => setIsRecording(false);
    
    recognitionRef.current = recognition;
    recognition.start();
  };

  const stop = () => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
      setIsRecording(false);
    }
  };

  return {
    isRecording,
    transcript,
    start,
    stop,
  };
}
