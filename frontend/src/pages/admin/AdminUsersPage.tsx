/**
 * AdminUsersPage — user management for administrators.
 *
 * Provides server-side search, role/status filters, and pagination over
 * the user list, plus activate/deactivate toggles, a details modal, and
 * permanent deletion.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Search, Users, RefreshCw, Eye, Ban, CheckCircle2, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminService } from '../../services/admin.service';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Modal } from '../../components/ui/Modal';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ConfirmDeleteModal } from '../../components/admin/ConfirmDeleteModal';
import { AdminPagination } from '../../components/admin/AdminPagination';
import { formatDate } from '../../utils/date';
import { formatNumber } from '../../utils/format';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { AdminUserResponse } from '../../types/admin';

const PAGE_SIZE = 10;

export const AdminUsersPage: React.FC = () => {
  // Data state
  const [users, setUsers] = useState<AdminUserResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter state — the immediate inputs and their debounced copies. The
  // debounced values drive the query so typing does not fire a request per
  // keystroke, and changing any filter resets to the first page.
  const [filters, setFilters] = useState({ search: '', role: '', active: '' });
  const [debouncedFilters, setDebouncedFilters] = useState(filters);
  const isFirstRender = useRef(true);

  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }
    const timer = setTimeout(() => {
      setDebouncedFilters(filters);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [filters]);

  // Action state
  const [details, setDetails] = useState<AdminUserResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<AdminUserResponse | null>(null);
  const [statusTarget, setStatusTarget] = useState<AdminUserResponse | null>(null);
  const [isActing, setIsActing] = useState(false);

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getUsers({
        page,
        size: PAGE_SIZE,
        search: debouncedFilters.search || undefined,
        role: debouncedFilters.role || undefined,
        active:
          debouncedFilters.active === '' ? undefined : debouncedFilters.active === 'true',
      });
      setUsers(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('users')));
    } finally {
      setIsLoading(false);
    }
  }, [page, debouncedFilters]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const hasFilters =
    filters.search.trim() !== '' || filters.role !== '' || filters.active !== '';

  const handleResetFilters = () => {
    setFilters({ search: '', role: '', active: '' });
    setPage(0);
  };

  const handleToggleStatus = async () => {
    if (!statusTarget) return;
    setIsActing(true);
    try {
      const updated = await adminService.setUserActive(statusTarget.id, !statusTarget.isActive);
      toast.success(
        updated.isActive
          ? `User ${updated.email} has been activated.`
          : `User ${updated.email} has been deactivated.`,
      );
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
      setStatusTarget(null);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('user status')));
    } finally {
      setIsActing(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setIsActing(true);
    try {
      await adminService.deleteUser(deleteTarget.id);
      toast.success(MESSAGES.DELETE_SUCCESS('User'));
      setUsers((prev) => prev.filter((u) => u.id !== deleteTarget.id));
      setTotalElements((prev) => prev - 1);
      setDeleteTarget(null);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('user deletion')));
    } finally {
      setIsActing(false);
    }
  };

  const roleBadgeVariant = useMemo(
    () =>
      ({
        ADMIN: 'primary',
        STUDENT: 'default',
      }) as const,
    [],
  );

  if (isLoading && users.length === 0) {
    return <LoadingScreen />;
  }

  return (
    <div className="mx-auto max-w-6xl">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">User Management</h1>
          <p className="mt-1 text-sm text-gray-500">
            {formatNumber(totalElements)} registered {totalElements === 1 ? 'user' : 'users'}.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchUsers}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Filters */}
      <div className="mb-6 grid gap-3 rounded-xl border border-gray-200 bg-white p-4 shadow-sm sm:grid-cols-2 lg:grid-cols-4">
        <div className="lg:col-span-2">
          <Input
            label="Search"
            placeholder="Name or email…"
            leftIcon={<Search className="h-4 w-4" />}
            value={filters.search}
            onChange={(e) => setFilters((prev) => ({ ...prev, search: e.target.value }))}
          />
        </div>
        <div>
          <label className="mb-1.5 block text-sm font-medium text-gray-700">Role</label>
          <select
            value={filters.role}
            onChange={(e) => setFilters((prev) => ({ ...prev, role: e.target.value }))}
            className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="">All roles</option>
            <option value="STUDENT">Student</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>
        <div>
          <label className="mb-1.5 block text-sm font-medium text-gray-700">Status</label>
          <select
            value={filters.active}
            onChange={(e) => setFilters((prev) => ({ ...prev, active: e.target.value }))}
            className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="">All statuses</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>
        </div>
      </div>

      {/* Error state */}
      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} onRetry={fetchUsers} />
        </div>
      )}

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-gray-100 bg-gray-50">
              <tr className="text-xs uppercase tracking-wide text-gray-400">
                <th className="px-4 py-3 font-medium">User</th>
                <th className="px-4 py-3 font-medium">Role</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">Registered</th>
                <th className="px-4 py-3 text-right font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {users.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-12 text-center">
                    <Users className="mx-auto mb-3 h-10 w-10 text-gray-300" />
                    <p className="text-sm font-medium text-gray-500">No users found</p>
                    {hasFilters && (
                      <button
                        onClick={handleResetFilters}
                        className="mt-2 text-xs font-medium text-indigo-600 hover:text-indigo-800"
                      >
                        Clear all filters
                      </button>
                    )}
                  </td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id} className="transition-colors hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-primary-100 text-xs font-semibold text-primary-600">
                          {user.firstName.charAt(0)}
                          {user.lastName.charAt(0)}
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">
                            {user.firstName} {user.lastName}
                          </p>
                          <p className="text-xs text-gray-400">{user.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant={roleBadgeVariant[user.role as 'ADMIN' | 'STUDENT'] ?? 'default'} size="sm">
                        {user.role}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant={user.isActive ? 'success' : 'danger'} size="sm">
                        {user.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </td>
                    <td className="px-4 py-3 text-xs text-gray-400">
                      {formatDate(user.createdAt)}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => setDetails(user)}
                          title="View details"
                          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700"
                        >
                          <Eye className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => setStatusTarget(user)}
                          title={user.isActive ? 'Deactivate' : 'Activate'}
                          className={`rounded-lg p-1.5 transition-colors hover:bg-gray-100 ${
                            user.isActive ? 'text-amber-500 hover:text-amber-600' : 'text-emerald-500 hover:text-emerald-600'
                          }`}
                        >
                          {user.isActive ? <Ban className="h-4 w-4" /> : <CheckCircle2 className="h-4 w-4" />}
                        </button>
                        <button
                          onClick={() => setDeleteTarget(user)}
                          title="Delete user"
                          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="border-t border-gray-100 px-4 py-3">
          <AdminPagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={setPage}
          />
        </div>
      </div>

      {/* Details modal */}
      <Modal isOpen={details !== null} onClose={() => setDetails(null)} title="User Details">
        {details && (
          <dl className="space-y-3 text-sm">
            {[
              ['Name', `${details.firstName} ${details.lastName}`],
              ['Email', details.email],
              ['Phone', details.phone || '—'],
              ['Role', details.role],
              ['Status', details.isActive ? 'Active' : 'Inactive'],
              ['Registered', formatDate(details.createdAt)],
            ].map(([label, value]) => (
              <div key={label} className="flex justify-between gap-4">
                <dt className="text-gray-400">{label}</dt>
                <dd className="text-right font-medium text-gray-900">{value}</dd>
              </div>
            ))}
          </dl>
        )}
      </Modal>

      {/* Status confirmation modal */}
      <Modal
        isOpen={statusTarget !== null}
        onClose={() => setStatusTarget(null)}
        title={statusTarget?.isActive ? 'Deactivate User' : 'Activate User'}
        closeOnBackdrop={false}
      >
        <p className="text-sm text-gray-600">
          {statusTarget?.isActive
            ? 'This user will no longer be able to sign in to their account.'
            : 'This user will regain access to their account.'}
        </p>
        <div className="mt-6 flex justify-end gap-2">
          <Button variant="ghost" onClick={() => setStatusTarget(null)} disabled={isActing}>
            Cancel
          </Button>
          <Button
            variant={statusTarget?.isActive ? 'danger' : 'primary'}
            onClick={handleToggleStatus}
            loading={isActing}
          >
            {statusTarget?.isActive ? 'Deactivate' : 'Activate'}
          </Button>
        </div>
      </Modal>

      {/* Delete confirmation modal */}
      <ConfirmDeleteModal
        isOpen={deleteTarget !== null}
        resource="user"
        targetName={deleteTarget ? `${deleteTarget.firstName} ${deleteTarget.lastName}` : undefined}
        isLoading={isActing}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
};
