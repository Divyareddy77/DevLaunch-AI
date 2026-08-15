/**
 * LanguageChart — visualises a GitHub user's language usage.
 *
 * Reuses the generic PieChart component (Recharts) to render the
 * language distribution as a donut chart, alongside a ranked list
 * showing each language with its repository count and share of the
 * total.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Code2 } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { PieChart, type PieChartDataPoint } from '../charts';
import type { LanguageStatisticsResponse } from '../../types/github';

interface LanguageChartProps {
  /** Language usage statistics sorted by repository count descending. */
  languages: LanguageStatisticsResponse[];
  /** Whether the chart data is still loading. */
  loading?: boolean;
}

/** GitHub-brand colours for well-known languages. Falls back to the chart palette. */
const LANGUAGE_COLORS: Record<string, string> = {
  TypeScript: '#3178c6',
  JavaScript: '#f1e05a',
  Java: '#b07219',
  Python: '#3572a5',
  'C++': '#f34b7d',
  'C#': '#178600',
  C: '#555555',
  Go: '#00add8',
  Rust: '#dea584',
  Ruby: '#701516',
  PHP: '#4f5d95',
  Swift: '#f05138',
  Kotlin: '#a97bff',
  HTML: '#e34c26',
  CSS: '#563d7c',
  Shell: '#89e051',
  Dart: '#00b4ab',
  Vue: '#41b883',
  'Jupyter Notebook': '#da5b0b',
};

export const LanguageChart: React.FC<LanguageChartProps> = ({ languages, loading = false }) => {
  const total = languages.reduce((sum, language) => sum + language.repositoryCount, 0);

  const chartData: PieChartDataPoint[] = languages.map((language) => ({
    name: language.language,
    value: language.repositoryCount,
    color: LANGUAGE_COLORS[language.language],
  }));

  return (
    <Card
      className="overflow-hidden"
      header={
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Code2 className="h-5 w-5 text-indigo-500" />
            <h2 className="text-lg font-semibold text-gray-900">Language Analysis</h2>
          </div>
          {languages.length > 0 && (
            <Badge variant="primary">
              {languages.length} {languages.length === 1 ? 'language' : 'languages'}
            </Badge>
          )}
        </div>
      }
    >
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Donut chart */}
        <div className="flex items-center justify-center">
          <PieChart data={chartData} loading={loading} innerRadius={55} className="w-full" />
        </div>

        {/* Ranked language list */}
        <div className="flex flex-col justify-center">
          {loading ? (
            <div className="space-y-3 animate-pulse">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="space-y-1.5">
                  <div className="h-4 w-1/3 rounded bg-gray-200" />
                  <div className="h-1.5 w-full rounded bg-gray-200" />
                </div>
              ))}
            </div>
          ) : languages.length === 0 ? (
            <p className="text-center text-sm text-gray-400">No language data available.</p>
          ) : (
            <ul className="space-y-4">
              {languages.map((language) => {
                const color = LANGUAGE_COLORS[language.language] ?? '#6366f1';
                const share = total > 0 ? (language.repositoryCount / total) * 100 : 0;

                return (
                  <li key={language.language}>
                    <div className="flex items-center justify-between text-sm">
                      <span className="inline-flex items-center gap-2 font-medium text-gray-700">
                        <span
                          className="h-2.5 w-2.5 rounded-full"
                          style={{ backgroundColor: color }}
                        />
                        {language.language}
                      </span>
                      <span className="text-gray-500">
                        {language.repositoryCount}{' '}
                        {language.repositoryCount === 1 ? 'repo' : 'repos'} · {share.toFixed(0)}%
                      </span>
                    </div>
                    <div className="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
                      <div
                        className="h-full rounded-full transition-all duration-700"
                        style={{ width: `${share}%`, backgroundColor: color }}
                      />
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </div>
    </Card>
  );
};
