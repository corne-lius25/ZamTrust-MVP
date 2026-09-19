export function Skeleton({ className = '' }: { className?: string }) {
  return <div className={'animate-pulse rounded-input bg-surface-muted ' + className} />;
}

export function TableSkeleton({ rows = 5 }: { rows?: number }) {
  return (
    <div className="divide-y divide-border">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="grid grid-cols-12 items-center gap-4 px-6 py-4">
          <div className="col-span-5 space-y-2">
            <Skeleton className="h-3.5 w-3/4" />
            <Skeleton className="h-3 w-1/3" />
          </div>
          <div className="col-span-3">
            <Skeleton className="h-5 w-20" />
          </div>
          <div className="col-span-3">
            <Skeleton className="h-3 w-2/3" />
          </div>
          <div className="col-span-1 flex justify-end">
            <Skeleton className="h-3 w-8" />
          </div>
        </div>
      ))}
    </div>
  );
}
