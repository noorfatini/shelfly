// The one signature visual element: a rotated "ink stamp" badge for borrowing status,
// echoing the due-date stamp on the inside cover of a real library book.
const LABELS = {
  BORROWED: "Borrowed",
  RETURNED: "Returned",
  OVERDUE: "Overdue",
  ACTIVE: "Active",
  INACTIVE: "Inactive",
};

export default function StatusStamp({ status }) {
  const cls = `status-stamp status-${status?.toLowerCase()}`;
  return <span className={cls}>{LABELS[status] || status}</span>;
}
