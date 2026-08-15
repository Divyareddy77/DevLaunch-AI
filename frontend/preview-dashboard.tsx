/**
 * LIVE PREVIEW HARNESS (not part of the application).
 *
 * Renders the redesigned dashboard — hero, placement readiness, KPI grid,
 * Analytics & Insights, and detail widgets — with representative mock data
 * so the UI can be reviewed without the Spring Boot backend. The API layer
 * is mocked with an axios adapter; no real backend is required.
 *
 * Reproduce these two files (preview-dashboard.html + preview-dashboard.tsx)
 * to re-create the preview; see .freebuff/run.md.
 */
import React from 'react';
import { createRoot } from 'react-dom/client';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import './src/index.css';
import apiClient from './src/api/client';
import { DashboardHero } from './src/components/dashboard/DashboardHero';
import { PlacementReadinessCard } from './src/components/dashboard/PlacementReadinessCard';
import { KpiCard } from './src/components/dashboard/KpiCard';
import { CountUp } from './src/components/ui/CountUp';
import { AnalyticsSection } from './src/components/dashboard/analytics/AnalyticsSection';
import { JobApplicationCard } from './src/components/dashboard/JobApplicationCard';
import { MockInterviewCard } from './src/components/dashboard/MockInterviewCard';
import { StudyPlannerCard as DashboardStudyPlannerCard } from './src/components/dashboard/StudyPlannerCard';
import { GitHubCard } from './src/components/dashboard/GitHubCard';
import { LeetCodeCard } from './src/components/dashboard/LeetCodeCard';
import { AchievementsWidget } from './src/components/dashboard/ux/AchievementsWidget';
import { QuickActionsPanel } from './src/components/dashboard/ux/QuickActionsPanel';
import { AiRecommendations } from './src/components/dashboard/ux/AiRecommendations';
import { ActivityTimeline } from './src/components/dashboard/ux/ActivityTimeline';
import { NotificationsWidget } from './src/components/dashboard/ux/NotificationsWidget';
import { AnnouncementBanner } from './src/components/announcement/AnnouncementBanner';
import { PageHeader } from './src/components/shared/PageHeader';
import { ResumeCard } from './src/components/resume/ResumeCard';
import { StudyPlannerCard } from './src/components/study-planner/StudyPlannerCard';
import { CalendarView } from './src/components/study-planner/CalendarView';
import type { ResumeResponse } from './src/types/resume';
import {
  ShieldCheck,
  FileText,
  Briefcase,
  Mic,
  CalendarCheck,
  Github,
  Code2,
  Bell,
} from 'lucide-react';
import type { DashboardResponse } from './src/types/dashboard';
import type { AchievementSummary, XpHistoryEntry } from './src/types/achievement';
import type { InterviewHistoryResponse } from './src/types/ai';
import type { ApplicationAnalytics } from './src/types/job-application';
import type { StudyPlannerResponse } from './src/types/study-planner';
import type { RepositoryResponse, LanguageStatisticsResponse } from './src/types/github';
import type { AppNotification } from './src/types/notification';
import type { Announcement } from './src/types/announcement';

const mockData: DashboardResponse = {
  resumeCompletion: 85,
  totalJobApplications: 12,
  mockInterviewCount: 8,
  mockInterviewLatestScore: 88,
  mockInterviewAverageScore: 74,
  mockInterviewBestScore: 88,
  mockInterviewTrend: 6,
  mockInterviewInsight:
    'Your technical answers are strong — try structuring responses with STAR to lift clarity further.',
  interviewApplications: 3,
  offerApplications: 2,
  assessmentApplications: 2,
  recentApplications: [
    { id: 1, companyName: 'Google', jobRole: 'Software Engineer', status: 'INTERVIEW', applicationDate: '2026-08-06' },
    { id: 2, companyName: 'Stripe', jobRole: 'Backend Engineer', status: 'ASSESSMENT', applicationDate: '2026-08-05' },
    { id: 3, companyName: 'Atlassian', jobRole: 'Java Developer', status: 'APPLIED', applicationDate: '2026-08-03' },
  ],
  upcomingInterview: {
    applicationId: 1,
    companyName: 'Google',
    jobRole: 'Software Engineer',
    scheduledDate: '2026-08-10',
    scheduledTime: '14:00:00',
  },
  totalStudyTasks: 18,
  completedStudyTasks: 11,
  githubUsername: 'divyareddy',
  githubRepositories: 13,
  githubTopLanguage: 'Java',
  githubFollowers: 23,
  githubFollowing: 41,
  leetcodeUsername: 'divya_reddy',
  leetcodeSolved: 137,
  leetcodeEasySolved: 62,
  leetcodeMediumSolved: 61,
  leetcodeHardSolved: 14,
  leetcodeRanking: 482110,
  leetcodeAcceptanceRate: 72.4,
  placementReadiness: 78,
  atsScore: 86,
  atsReviewedAt: '2026-08-05T10:00:00',
  resumeQualityStatus: 'Excellent',
  readinessStatus: 'Placement Ready',
  readinessPrevious: 71,
  readinessChange: 7,
  readinessUpdatedAt: '2026-08-07T09:00:00',
  readinessStrongestArea: 'Resume ATS',
  readinessWeakestArea: 'LeetCode',
  readinessNextGoal:
    'Solve 8 more medium LeetCode problems this week to lift your readiness to 80+.',
  readinessModules: [
    { key: 'RESUME_ATS', label: 'Resume ATS', value: '86 / 100', score: 86 },
    { key: 'RESUME_COMPLETION', label: 'Resume Completion', value: '85%', score: 85 },
    { key: 'MOCK_INTERVIEW', label: 'Mock Interview', value: '74 avg', score: 74 },
    { key: 'GITHUB', label: 'GitHub', value: '13 repos', score: 64 },
    { key: 'LEETCODE', label: 'LeetCode', value: '137 solved', score: 58 },
    { key: 'STUDY_PLANNER', label: 'Study Planner', value: '61% done', score: 61 },
    { key: 'JOB_APPLICATIONS', label: 'Job Applications', value: '12 applied', score: 72 },
  ],
  readinessStrengths: ['Strong ATS-optimised resume', 'Consistent interview practice', 'Active application pipeline'],
  readinessImprovements: ['LeetCode coverage is thin', 'Study plan completion slipping'],
  readinessRecommendations: [
    'Schedule 3 mock interviews this week',
    'Re-run the AI resume review after edits',
    'Push GitHub activity to 5 commits per week',
  ],
};

const mockSummary: AchievementSummary = {
  level: 7,
  levelTitle: 'Job Seeker Pro',
  totalXp: 4850,
  currentLevelXp: 3600,
  nextLevelXp: 5200,
  nextLevel: 8,
  xpIntoLevel: 1250,
  xpNeededForNext: 1600,
  levelProgressPercent: 78,
  totalAchievements: 24,
  unlockedCount: 15,
  lockedCount: 9,
  completionPercent: 63,
  latestUnlock: {
    id: 3,
    code: 'ATS_EXPERT',
    category: 'RESUME',
    title: 'ATS Expert',
    description: 'Score 80+ on an AI resume review',
    icon: '🛡️',
    color: '#6366f1',
    xpReward: 150,
    unlockedAt: '2026-08-04T10:00:00',
  },
  recentUnlocks: [],
};

const interviewHistory: InterviewHistoryResponse = {
  history: [
    { sessionId: 's1', interviewType: 'HR', overallScore: 62, confidenceScore: 58, communicationScore: 66, clarityScore: 61, durationSeconds: 300, wordCount: 480, completedAt: '2026-05-14T10:00:00', strengths: [], areasForImprovement: [], feedback: [] },
    { sessionId: 's2', interviewType: 'JAVA', overallScore: 68, confidenceScore: 63, communicationScore: 70, clarityScore: 66, durationSeconds: 330, wordCount: 610, completedAt: '2026-05-28T15:30:00', strengths: [], areasForImprovement: [], feedback: [] },
    { sessionId: 's3', interviewType: 'SQL', overallScore: 71, confidenceScore: 67, communicationScore: 72, clarityScore: 70, durationSeconds: 300, wordCount: 560, completedAt: '2026-06-11T09:00:00', strengths: [], areasForImprovement: [], feedback: [] },
    { sessionId: 's4', interviewType: 'REACT', overallScore: 76, confidenceScore: 72, communicationScore: 75, clarityScore: 74, durationSeconds: 360, wordCount: 730, completedAt: '2026-06-25T14:00:00', strengths: [], areasForImprovement: [], feedback: [] },
    { sessionId: 's5', interviewType: 'SPRING_BOOT', overallScore: 74, confidenceScore: 70, communicationScore: 76, clarityScore: 72, durationSeconds: 330, wordCount: 640, completedAt: '2026-07-09T11:30:00', strengths: [], areasForImprovement: [], feedback: [] },
    { sessionId: 's6', interviewType: 'JAVA', overallScore: 81, confidenceScore: 78, communicationScore: 80, clarityScore: 79, durationSeconds: 390, wordCount: 830, completedAt: '2026-07-23T16:00:00', strengths: [], areasForImprovement: [], feedback: [] },
    { sessionId: 's7', interviewType: 'HR', overallScore: 84, confidenceScore: 82, communicationScore: 85, clarityScore: 83, durationSeconds: 360, wordCount: 900, completedAt: '2026-08-01T10:30:00', strengths: [], areasForImprovement: [], feedback: [] },
    { sessionId: 's8', interviewType: 'REACT', overallScore: 88, confidenceScore: 86, communicationScore: 88, clarityScore: 87, durationSeconds: 420, wordCount: 1050, completedAt: '2026-08-06T15:00:00', strengths: [], areasForImprovement: [], feedback: [] },
  ],
  totalInterviews: 8,
  averageScore: 75.5,
  bestScore: 88,
  lastInterviewAt: '2026-08-06T15:00:00',
  currentStreak: 6,
  totalTimeSpentSeconds: 2790,
  totalQuestionsAnswered: 40,
  successRate: 62.5,
  readinessLevel: 'Interview Ready',
};

const jobAnalytics: ApplicationAnalytics = {
  totalApplications: 12,
  statusCounts: { APPLIED: 4, ASSESSMENT: 2, INTERVIEW: 3, OFFER: 2, REJECTED: 1 },
  monthlyApplications: [
    { yearMonth: '2026-04', count: 2 },
    { yearMonth: '2026-05', count: 3 },
    { yearMonth: '2026-06', count: 3 },
    { yearMonth: '2026-07', count: 4 },
  ],
  interviewRate: 25,
  offerRate: 16.7,
  rejectionRate: 8.3,
  successRate: 41.7,
  averageResponseTimeDays: 6,
  activeInterviews: 3,
  upcomingInterviews: [],
};

const studyTasks: StudyPlannerResponse[] = [
  { id: 1, title: 'DSA: Trees', description: null, studyDate: '2026-08-01', startTime: '09:00:00', endTime: '10:30:00', priority: 'HIGH', status: 'COMPLETED' },
  { id: 2, title: 'Java Concurrency', description: null, studyDate: '2026-08-01', startTime: '18:00:00', endTime: '19:30:00', priority: 'MEDIUM', status: 'COMPLETED' },
  { id: 3, title: 'SQL Window Functions', description: null, studyDate: '2026-08-02', startTime: '10:00:00', endTime: '11:30:00', priority: 'MEDIUM', status: 'COMPLETED' },
  { id: 4, title: 'Spring Boot Actuator', description: null, studyDate: '2026-08-03', startTime: '09:30:00', endTime: '11:00:00', priority: 'HIGH', status: 'COMPLETED' },
  { id: 5, title: 'System Design Basics', description: null, studyDate: '2026-08-03', startTime: '17:00:00', endTime: '18:30:00', priority: 'HIGH', status: 'IN_PROGRESS' },
  { id: 6, title: 'LeetCode: Graphs', description: null, studyDate: '2026-08-04', startTime: '10:00:00', endTime: '12:00:00', priority: 'HIGH', status: 'COMPLETED' },
  { id: 7, title: 'React Hooks Deep Dive', description: null, studyDate: '2026-08-04', startTime: '19:00:00', endTime: '20:00:00', priority: 'LOW', status: 'PENDING' },
  { id: 8, title: 'Mock Interview Prep', description: null, studyDate: '2026-08-05', startTime: '11:00:00', endTime: '12:30:00', priority: 'MEDIUM', status: 'COMPLETED' },
  { id: 9, title: 'Kafka Fundamentals', description: null, studyDate: '2026-08-06', startTime: '09:00:00', endTime: '10:30:00', priority: 'HIGH', status: 'COMPLETED' },
  { id: 10, title: 'AWS S3 + Lambda', description: null, studyDate: '2026-08-07', startTime: '10:00:00', endTime: '11:30:00', priority: 'MEDIUM', status: 'COMPLETED' },
  { id: 11, title: 'Microservices Patterns', description: null, studyDate: '2026-08-07', startTime: '17:30:00', endTime: '19:00:00', priority: 'HIGH', status: 'PENDING' },
];

const repos: RepositoryResponse[] = [
  { name: 'devlaunch-api', description: null, language: 'Java', stars: 4, forks: 1, repositoryUrl: '#', createdAt: '2025-09-12T08:00:00', updatedAt: '2026-08-01T08:00:00' },
  { name: 'leetcode-solutions', description: null, language: 'Java', stars: 2, forks: 0, repositoryUrl: '#', createdAt: '2025-11-03T08:00:00', updatedAt: '2026-08-02T08:00:00' },
  { name: 'portfolio', description: null, language: 'TypeScript', stars: 1, forks: 0, repositoryUrl: '#', createdAt: '2026-01-18T08:00:00', updatedAt: '2026-08-03T08:00:00' },
  { name: 'spring-boot-starter-kit', description: null, language: 'Java', stars: 6, forks: 2, repositoryUrl: '#', createdAt: '2026-02-25T08:00:00', updatedAt: '2026-08-04T08:00:00' },
  { name: 'sql-playground', description: null, language: 'SQL', stars: 1, forks: 0, repositoryUrl: '#', createdAt: '2026-04-10T08:00:00', updatedAt: '2026-08-05T08:00:00' },
  { name: 'react-dashboard', description: null, language: 'TypeScript', stars: 3, forks: 1, repositoryUrl: '#', createdAt: '2026-05-22T08:00:00', updatedAt: '2026-08-06T08:00:00' },
  { name: 'interview-prep', description: null, language: 'Java', stars: 2, forks: 0, repositoryUrl: '#', createdAt: '2026-07-15T08:00:00', updatedAt: '2026-08-07T08:00:00' },
];

const languages: LanguageStatisticsResponse[] = [
  { language: 'Java', repositoryCount: 4 },
  { language: 'TypeScript', repositoryCount: 2 },
  { language: 'SQL', repositoryCount: 1 },
];

const xpHistory: XpHistoryEntry[] = [
  { id: 11, amount: 150, reason: 'ACHIEVEMENT_UNLOCKED', description: 'Unlocked the ATS Expert badge', createdAt: '2026-08-06T15:30:00' },
  { id: 10, amount: 40, reason: 'INTERVIEW_COMPLETED', description: 'Completed a React mock interview', createdAt: '2026-08-06T15:00:00' },
  { id: 9, amount: 25, reason: 'STUDY_TASK_COMPLETED', description: 'Finished Kafka Fundamentals session', createdAt: '2026-08-06T10:30:00' },
  { id: 8, amount: 30, reason: 'JOB_APPLICATION_CREATED', description: 'Applied to Google — Software Engineer', createdAt: '2026-08-06T09:00:00' },
  { id: 7, amount: 35, reason: 'RESUME_REVIEWED', description: 'Ran an AI resume review', createdAt: '2026-08-05T10:00:00' },
  { id: 6, amount: 25, reason: 'LEETCODE_SYNCED', description: 'Synced LeetCode progress', createdAt: '2026-08-04T18:00:00' },
  { id: 5, amount: 20, reason: 'STUDY_TASK_COMPLETED', description: 'Finished LeetCode: Graphs session', createdAt: '2026-08-04T12:00:00' },
  { id: 4, amount: 30, reason: 'GITHUB_CONNECTED', description: 'Linked GitHub account', createdAt: '2026-08-03T09:30:00' },
];

const mockNotifications: AppNotification[] = [
  { id: 1, title: 'ATS Expert badge unlocked', message: 'You scored 80+ on an AI resume review — the ATS Expert badge is yours!', type: 'ACHIEVEMENT', isRead: false, createdAt: '2026-08-06T15:31:00' },
  { id: 2, title: 'Interview feedback ready', message: 'Your React mock interview report is available with full feedback.', type: 'MOCK_INTERVIEW', isRead: false, createdAt: '2026-08-06T15:05:00' },
  { id: 3, title: 'Upcoming interview', message: 'Google — Software Engineer is scheduled for Aug 10 at 2:00 PM.', type: 'JOB', isRead: true, createdAt: '2026-08-05T09:00:00' },
  { id: 4, title: 'Placement readiness updated', message: 'Your readiness score rose to 78 — up 7 points from last week.', type: 'READINESS', isRead: true, createdAt: '2026-08-04T09:00:00' },
];

const mockResumes: ResumeResponse[] = [
  {
    id: 1,
    headline: 'Senior Full-Stack Engineer — Java & React',
    summary:
      'Product-minded engineer with 5+ years building scalable web platforms, REST APIs, and real-time dashboards for fintech and edtech teams.',
    linkedinUrl: 'https://linkedin.com/in/divyareddy',
    githubUrl: 'https://github.com/divyareddy',
    portfolioUrl: 'https://divyareddy.dev',
    template: { id: 1, name: 'Modern Blue', description: null, previewImageUrl: null },
  },
  {
    id: 2,
    headline: 'Backend Engineer — Spring Boot & Cloud',
    summary:
      'Backend specialist focused on microservices, event-driven architecture, and cost-efficient AWS deployments.',
    linkedinUrl: 'https://linkedin.com/in/divyareddy',
    githubUrl: null,
    portfolioUrl: null,
    template: { id: 2, name: 'Classic Professional', description: null, previewImageUrl: null },
  },
  {
    id: 3,
    headline: 'Full-Stack Developer Resume',
    summary: 'Versatile developer with experience across the full stack.',
    linkedinUrl: null,
    githubUrl: null,
    portfolioUrl: null,
    template: null,
  },
];

const mockAnnouncements: Announcement[] = [
  { id: 1, title: 'New AI interview categories', content: 'Try the new SQL and System Design mock interview tracks with relevance-aware scoring.', isActive: true, createdById: 1, createdByEmail: 'admin@devlaunch.app', createdByName: 'DevLaunch Team', createdAt: '2026-08-06T08:00:00', updatedAt: '2026-08-06T08:00:00' },
  { id: 2, title: 'Resume templates refreshed', content: 'Five new ATS-optimised templates with one-click PDF export are now live.', isActive: true, createdById: 1, createdByEmail: 'admin@devlaunch.app', createdByName: 'DevLaunch Team', createdAt: '2026-08-01T08:00:00', updatedAt: '2026-08-01T08:00:00' },
  { id: 3, title: 'Placement readiness v2', content: 'Readiness now scores every module and recommends your next best move.', isActive: true, createdById: 1, createdByEmail: 'admin@devlaunch.app', createdByName: 'DevLaunch Team', createdAt: '2026-07-28T08:00:00', updatedAt: '2026-07-28T08:00:00' },
];

// Mock the API layer so the Analytics & Insights section fetches resolve.
const MOCK_ROUTES: Record<string, unknown> = {
  '/api/job-applications/analytics': jobAnalytics,
  '/api/study-planners': studyTasks,
  '/api/github/divyareddy/repositories': repos,
  '/api/github/divyareddy/languages': languages,
  '/api/achievements/history': xpHistory,
  '/api/notifications': mockNotifications,
  '/api/announcements/active': mockAnnouncements,
};

apiClient.defaults.adapter = async (config) => {
  const url = config.url ?? '';
  if (url in MOCK_ROUTES) {
    return {
      data: MOCK_ROUTES[url],
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    } as never;
  }
  throw new Error(`No mock registered for ${url}`);
};

function App() {
  return (
    <div className="min-h-screen bg-gray-50 p-4 lg:p-6">
      <div className="mx-auto max-w-7xl space-y-8">
        {/* Hero banner */}
        <DashboardHero firstName="Divya" data={mockData} summary={mockSummary} streak={6} />

        {/* Announcements carousel */}
        <AnnouncementBanner announcements={mockAnnouncements} />

        {/* Quick statistics KPI grid */}
        <section>
          <div className="mb-4">
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">Quick Statistics</h2>
            <p className="text-sm text-gray-500">Your career preparation at a glance</p>
          </div>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-4">
            <KpiCard label="ATS Score" icon={<ShieldCheck className="h-5 w-5" />} tone="violet" value={<CountUp value={86} className="text-emerald-600" />} subtitle="Excellent · reviewed Aug 5, 2026" progress={86} barTone="success" />
            <KpiCard label="Resume Completion" icon={<FileText className="h-5 w-5" />} tone="success" value={<CountUp value={85} suffix="%" />} subtitle="4 of 5 fields filled" progress={85} />
            <KpiCard label="Job Applications" icon={<Briefcase className="h-5 w-5" />} tone="orange" value={<CountUp value={12} />} subtitle="3 interviews · 2 offers" progress={25} />
            <KpiCard label="Mock Interviews" icon={<Mic className="h-5 w-5" />} tone="violet" value={<CountUp value={8} />} subtitle="Avg 74 · Best 88" progress={74} />
            <KpiCard label="Study Planner" icon={<CalendarCheck className="h-5 w-5" />} tone="warning" value={<CountUp value={11} />} subtitle="61% complete · 18 total tasks" progress={61} />
            <KpiCard label="GitHub" icon={<Github className="h-5 w-5" />} tone="info" value={<CountUp value={13} />} subtitle="@divyareddy · 23 followers" />
            <KpiCard label="LeetCode" icon={<Code2 className="h-5 w-5" />} tone="danger" value={<CountUp value={137} />} subtitle="62E · 61M · 14H" progress={72.4} />
            <KpiCard label="Notifications" icon={<Bell className="h-5 w-5" />} tone="gray" value={<CountUp value={0} />} subtitle="All caught up" />
          </div>
        </section>

        {/* Placement readiness centrepiece */}
        <PlacementReadinessCard data={mockData} />

        {/* Analytics & Insights */}
        <AnalyticsSection
          data={mockData}
          history={interviewHistory}
          historyLoaded
          summary={mockSummary}
          streak={6}
        />

        {/* Recommendations & quick actions */}
        <section>
          <div className="mb-4">
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">Recommendations</h2>
            <p className="text-sm text-gray-500">AI-driven next steps and shortcuts to every module</p>
          </div>
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            <QuickActionsPanel />
            <AiRecommendations data={mockData} className="xl:col-span-2" />
          </div>
        </section>

        {/* Recent activity */}
        <section>
          <div className="mb-4">
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">Recent Activity</h2>
            <p className="text-sm text-gray-500">Review your latest momentum across the platform</p>
          </div>
          <ActivityTimeline />
        </section>

        {/* Detail widgets */}
        <section>
          <div className="mb-4">
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">Modules</h2>
            <p className="text-sm text-gray-500">Deep-dive into every part of DevLaunch</p>
          </div>
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            <JobApplicationCard
              totalApplications={mockData.totalJobApplications}
              interviewApplications={mockData.interviewApplications}
              offerApplications={mockData.offerApplications}
              assessmentApplications={mockData.assessmentApplications}
              recentApplications={mockData.recentApplications}
              upcomingInterview={mockData.upcomingInterview}
            />
            <MockInterviewCard
              count={mockData.mockInterviewCount}
              latestScore={mockData.mockInterviewLatestScore}
              averageScore={mockData.mockInterviewAverageScore}
              bestScore={mockData.mockInterviewBestScore}
              trend={mockData.mockInterviewTrend}
              insight={mockData.mockInterviewInsight}
              onViewAll={() => undefined}
            />
            <DashboardStudyPlannerCard
              totalTasks={mockData.totalStudyTasks}
              completedTasks={mockData.completedStudyTasks}
            />
            <GitHubCard
              connected
              username={mockData.githubUsername}
              repositoryCount={mockData.githubRepositories}
              topLanguage={mockData.githubTopLanguage}
              followers={mockData.githubFollowers}
              following={mockData.githubFollowing}
              onViewAll={() => undefined}
              onConnect={() => undefined}
            />
            <LeetCodeCard
              connected
              username={mockData.leetcodeUsername}
              totalSolved={mockData.leetcodeSolved}
              easySolved={mockData.leetcodeEasySolved}
              mediumSolved={mockData.leetcodeMediumSolved}
              hardSolved={mockData.leetcodeHardSolved}
              acceptanceRate={mockData.leetcodeAcceptanceRate}
              ranking={mockData.leetcodeRanking}
              onViewAll={() => undefined}
              onConnect={() => undefined}
            />
            <AchievementsWidget summary={mockSummary} loading={false} />
          </div>
        </section>

        {/* Notifications */}
        <section>
          <div className="mb-4">
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">Notifications</h2>
            <p className="text-sm text-gray-500">Stay on top of platform updates</p>
          </div>
          <NotificationsWidget unreadCount={2} />
        </section>

        {/* ─────── Module component showcase ─────── */}
        <section className="rounded-2xl border-2 border-dashed border-indigo-200 bg-white/60 p-6">
          <p className="mb-6 text-xs font-bold uppercase tracking-widest text-indigo-500">
            Module component showcase (preview harness)
          </p>

          {/* PageHeader demo */}
          <PageHeader
            title="Resume Builder"
            description="Build professional resumes that stand out to recruiters and ATS systems."
            badge={
              <span className="rounded-full bg-primary-100 px-3 py-1 text-xs font-semibold text-primary-700">
                3 resumes
              </span>
            }
            actions={<button className="rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-all hover:-translate-y-0.5 hover:bg-primary-700">Create Resume</button>}
          />

          {/* Resume cards */}
          <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
            {mockResumes.map((resume) => (
              <ResumeCard
                key={resume.id}
                resume={resume}
                onEdit={() => undefined}
                onDelete={() => undefined}
                onView={() => undefined}
                onDownload={() => undefined}
              />
            ))}
          </div>

          {/* Study planner demo */}
          <div className="mt-10 grid gap-6 lg:grid-cols-3">
            <div className="lg:col-span-1">
              <CalendarView
                taskDates={studyTasks.map((t) => t.studyDate)}
                selectedDate={null}
                onDateSelect={() => undefined}
                currentMonth={7}
                currentYear={2026}
                onPrevMonth={() => undefined}
                onNextMonth={() => undefined}
              />
            </div>
            <div className="grid content-start gap-4 sm:grid-cols-2 lg:col-span-2">
              {studyTasks.slice(0, 6).map((task) => (
                <StudyPlannerCard
                  key={task.id}
                  task={task}
                  onEdit={() => undefined}
                  onDelete={() => undefined}
                />
              ))}
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}

createRoot(document.getElementById('root')!).render(
  <MemoryRouter>
    <Routes>
      <Route path="*" element={<App />} />
    </Routes>
  </MemoryRouter>,
);
