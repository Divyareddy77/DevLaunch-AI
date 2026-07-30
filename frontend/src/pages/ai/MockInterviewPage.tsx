/**
 * MockInterviewPage — AI-powered mock interview practice.
 *
 * Allows users to select an interview category, answer generated
 * questions, and receive AI-driven feedback on their responses.
 *
 * This page is a scaffold for the future AI module. It will be
 * fully implemented once the backend AI endpoints are available.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Bot } from 'lucide-react';

export const MockInterviewPage: React.FC = () => {
  return (
    <div className="flex flex-col items-center justify-center py-16">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-indigo-100">
        <Bot className="h-8 w-8 text-indigo-600" />
      </div>
      <h1 className="mb-2 text-2xl font-bold text-gray-900">AI Mock Interview</h1>
      <p className="max-w-md text-center text-gray-500">
        Practice with AI-generated interview questions and receive
        personalised feedback to improve your performance.
      </p>
      <p className="mt-6 text-sm text-gray-400 italic">
        This feature is coming soon. The AI module will support categories
        including HR, Java, Spring Boot, SQL, and React.
      </p>
    </div>
  );
};
