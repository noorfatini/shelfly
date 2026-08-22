export default function LoadingState({ label = "Loading" }) {
  return (
    <div className="state-block loading-state">
      <div className="stamp-spinner" aria-hidden="true" />
      <p>{label}…</p>
    </div>
  );
}
