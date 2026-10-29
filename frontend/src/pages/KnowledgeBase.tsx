import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  BookOpen,
  Search,
  ThumbsUp,
  Eye,
  Plus,
  Tag,
  CheckCircle2,
  ExternalLink,
  Filter,
} from 'lucide-react';
import { knowledgeBaseApi } from '../api/apiService';
import { KnowledgeArticle, TicketCategory } from '../api/types';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';

const CATEGORIES: { label: string; value?: TicketCategory }[] = [
  { label: 'All Runbooks' },
  { label: 'Network', value: 'NETWORK' },
  { label: 'Access & AD', value: 'ACCESS' },
  { label: 'Outages & DB', value: 'OUTAGE' },
  { label: 'Security & SOC', value: 'SECURITY' },
  { label: 'Software & Apps', value: 'SOFTWARE' },
  { label: 'Hardware', value: 'HARDWARE' },
];

export const KnowledgeBase: React.FC = () => {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<TicketCategory | undefined>(undefined);
  const [activeArticle, setActiveArticle] = useState<KnowledgeArticle | null>(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);

  // Form state for creating a new article
  const [newTitle, setNewTitle] = useState('');
  const [newSummary, setNewSummary] = useState('');
  const [newCategory, setNewCategory] = useState<TicketCategory>('SOFTWARE');
  const [newTags, setNewTags] = useState('');
  const [newContent, setNewContent] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['knowledge-articles', searchTerm, selectedCategory],
    queryFn: () => knowledgeBaseApi.searchArticles(searchTerm || undefined, selectedCategory, 0, 30),
  });

  const voteMutation = useMutation({
    mutationFn: (id: number) => knowledgeBaseApi.voteHelpful(id),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['knowledge-articles'] });
      if (activeArticle && activeArticle.id === updated.id) {
        setActiveArticle(updated);
      }
    },
  });

  const createMutation = useMutation({
    mutationFn: () =>
      knowledgeBaseApi.createArticle({
        title: newTitle,
        summary: newSummary,
        category: newCategory,
        tags: newTags,
        content: newContent,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['knowledge-articles'] });
      setIsCreateOpen(false);
      setNewTitle('');
      setNewSummary('');
      setNewContent('');
      setNewTags('');
    },
  });

  const canCreate = user && user.role !== 'REQUESTER';

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <BookOpen className="w-7 h-7 text-indigo-600" />
            Knowledge Base & IT Runbooks
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Standard operating procedures, emergency playbooks, and verified resolution guides.
          </p>
        </div>
        {canCreate && (
          <button
            onClick={() => setIsCreateOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium shadow-sm transition-colors"
          >
            <Plus className="w-4 h-4" />
            New Runbook
          </button>
        )}
      </div>

      {/* Search and Filters */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <div className="relative">
          <Search className="w-5 h-5 absolute left-3.5 top-3 text-slate-400" />
          <input
            type="text"
            placeholder="Search runbooks by keyword, code (KB-1001), tags, or error message..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white"
          />
        </div>

        {/* Category Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 text-xs">
          {CATEGORIES.map((cat) => (
            <button
              key={cat.label}
              onClick={() => setSelectedCategory(cat.value)}
              className={`px-3 py-1.5 rounded-full font-medium whitespace-nowrap transition-colors ${
                selectedCategory === cat.value
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {cat.label}
            </button>
          ))}
        </div>
      </div>

      {/* Article Grid */}
      {isLoading ? (
        <div className="text-center py-12 text-slate-400">Loading runbooks...</div>
      ) : data?.content.length === 0 ? (
        <div className="bg-white rounded-xl border border-slate-200 p-12 text-center">
          <BookOpen className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-slate-700">No Runbooks Found</h3>
          <p className="text-xs text-slate-400 mt-1">Try adjusting your search or category filter.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {data?.content.map((article) => (
            <div
              key={article.id}
              onClick={() => setActiveArticle(article)}
              className="bg-white rounded-xl border border-slate-200 hover:border-indigo-300 hover:shadow-md transition-all p-5 flex flex-col justify-between cursor-pointer group"
            >
              <div className="space-y-2.5">
                <div className="flex items-center justify-between">
                  <span className="font-mono text-xs font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded border border-indigo-100">
                    {article.articleCode}
                  </span>
                  <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 bg-slate-100 px-2 py-0.5 rounded">
                    {article.category}
                  </span>
                </div>

                <h3 className="font-semibold text-slate-900 group-hover:text-indigo-600 transition-colors line-clamp-2">
                  {article.title}
                </h3>

                <p className="text-xs text-slate-600 line-clamp-3 leading-relaxed">
                  {article.summary}
                </p>

                {article.tags && (
                  <div className="flex items-center gap-1.5 flex-wrap pt-1">
                    <Tag className="w-3 h-3 text-slate-400" />
                    {article.tags.split(',').slice(0, 3).map((tag, i) => (
                      <span key={i} className="text-[10px] text-slate-500 bg-slate-50 px-1.5 py-0.5 rounded">
                        #{tag.trim()}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              <div className="flex items-center justify-between pt-4 mt-4 border-t border-slate-100 text-xs text-slate-400">
                <span className="flex items-center gap-1">
                  <Eye className="w-3.5 h-3.5" />
                  {article.viewCount} views
                </span>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    voteMutation.mutate(article.id);
                  }}
                  className="flex items-center gap-1 text-slate-500 hover:text-emerald-600 font-medium transition-colors"
                >
                  <ThumbsUp className="w-3.5 h-3.5" />
                  {article.helpfulCount} helpful
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* View Article Modal */}
      {activeArticle && (
        <Modal
          isOpen={!!activeArticle}
          onClose={() => setActiveArticle(null)}
          title={`${activeArticle.articleCode} — ${activeArticle.title}`}
          maxWidth="max-w-3xl"
        >
          <div className="space-y-4">
            <div className="flex items-center gap-3 text-xs text-slate-500 pb-3 border-b border-slate-100">
              <span className="bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded font-semibold">
                Category: {activeArticle.category}
              </span>
              <span>Author: {activeArticle.authorName || 'IT Ops'}</span>
              <span>·</span>
              <span>Updated: {new Date(activeArticle.updatedAt).toLocaleDateString()}</span>
            </div>

            <div className="bg-slate-50 p-3 rounded-lg border border-slate-200 text-xs text-slate-700">
              <strong className="text-slate-900 block mb-1">Issue Summary:</strong>
              {activeArticle.summary}
            </div>

            <div className="prose prose-slate max-w-none text-sm leading-relaxed whitespace-pre-wrap font-sans text-slate-800 bg-white p-4 rounded-lg border border-slate-100">
              {activeArticle.content}
            </div>

            <div className="flex items-center justify-between pt-4 border-t border-slate-100">
              <div className="flex items-center gap-2 text-xs text-slate-500">
                <Eye className="w-4 h-4 text-slate-400" />
                <span>{activeArticle.viewCount} team members viewed</span>
              </div>

              <button
                onClick={() => voteMutation.mutate(activeArticle.id)}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 rounded-lg text-xs font-semibold border border-emerald-200 transition-colors"
              >
                <ThumbsUp className="w-4 h-4" />
                Helpful ({activeArticle.helpfulCount})
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Create Runbook Modal */}
      {isCreateOpen && (
        <Modal
          isOpen={isCreateOpen}
          onClose={() => setIsCreateOpen(false)}
          title="Create New IT Runbook / Solution Guide"
          maxWidth="max-w-2xl"
        >
          <div className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Title</label>
              <input
                type="text"
                placeholder="e.g. Cisco AnyConnect Gateway Reset Procedure"
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Category</label>
                <select
                  value={newCategory}
                  onChange={(e) => setNewCategory(e.target.value as TicketCategory)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                >
                  <option value="NETWORK">Network</option>
                  <option value="ACCESS">Access & Identity</option>
                  <option value="OUTAGE">Outage & Database</option>
                  <option value="SECURITY">Security & SOC</option>
                  <option value="SOFTWARE">Software & Apps</option>
                  <option value="HARDWARE">Hardware</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Tags (comma separated)</label>
                <input
                  type="text"
                  placeholder="vpn, gateway, cisco"
                  value={newTags}
                  onChange={(e) => setNewTags(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Brief Summary</label>
              <input
                type="text"
                placeholder="1-2 sentences summarizing symptoms and applicability"
                value={newSummary}
                onChange={(e) => setNewSummary(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Detailed Content (Markdown)</label>
              <textarea
                rows={8}
                placeholder="### Diagnostic Steps&#10;1. Check ping to gateway...&#10;&#10;### Resolution Steps&#10;..."
                value={newContent}
                onChange={(e) => setNewContent(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 font-mono"
              />
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-slate-100">
              <button
                onClick={() => setIsCreateOpen(false)}
                className="px-4 py-2 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                Cancel
              </button>
              <button
                onClick={() => createMutation.mutate()}
                disabled={!newTitle || !newSummary || !newContent || createMutation.isPending}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-lg text-sm font-medium"
              >
                {createMutation.isPending ? 'Publishing...' : 'Publish Runbook'}
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
