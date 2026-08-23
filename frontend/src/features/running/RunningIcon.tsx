type IconName = 'route' | 'make' | 'run' | 'back' | 'location' | 'lock' | 'pause' | 'play' | 'stop' | 'camera' | 'chevron'

export function RunningIcon({ name, size = 24 }: { name: IconName; size?: number }) {
  const common = { width: size, height: size, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round' as const, strokeLinejoin: 'round' as const, 'aria-hidden': true }
  if (name === 'route') return <svg {...common}><circle cx="5" cy="18" r="2"/><circle cx="19" cy="6" r="2"/><path d="m7 17 4-5 3 2 3-6"/></svg>
  if (name === 'make') return <svg {...common}><path d="M4 17c3-7 7-8 11-3" strokeDasharray="3 3"/><circle cx="17" cy="17" r="5"/><path d="M17 14v6M14 17h6"/></svg>
  if (name === 'run') return <svg {...common}><circle cx="13" cy="4" r="2" fill="currentColor"/><path d="m12 7-3 5 4 2 2 6M9 12l-4 3M13 9l4 3M10 15l-3 5"/></svg>
  if (name === 'back') return <svg {...common}><path d="m15 18-6-6 6-6"/></svg>
  if (name === 'location') return <svg {...common}><circle cx="12" cy="12" r="3"/><circle cx="12" cy="12" r="8"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2"/></svg>
  if (name === 'lock') return <svg {...common}><rect x="6" y="10" width="12" height="10" rx="2"/><path d="M9 10V7a3 3 0 0 1 6 0v3M12 14v2"/></svg>
  if (name === 'pause') return <svg {...common}><path d="M9 6v12M15 6v12" strokeWidth="3"/></svg>
  if (name === 'play') return <svg {...common} fill="currentColor" stroke="none"><path d="m9 6 9 6-9 6z"/></svg>
  if (name === 'stop') return <svg {...common} fill="currentColor" stroke="none"><rect x="7" y="7" width="10" height="10"/></svg>
  if (name === 'camera') return <svg {...common}><path d="M5 8h3l1.5-2h5L16 8h3a2 2 0 0 1 2 2v8H3v-8a2 2 0 0 1 2-2Z"/><circle cx="12" cy="13" r="3"/></svg>
  return <svg {...common}><path d="m9 18 6-6-6-6"/></svg>
}
