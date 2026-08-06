// Landscape canvases keep their true (wide) shape. Portrait canvases are
// clamped to this minimum width:height ratio so a tall canvas doesn't tower
// over its landscape neighbours in the grid - it's still recognisably
// portrait, just not full-height.
const MIN_PORTRAIT_ASPECT_RATIO = 3 / 4

export function cardAspectRatio(widthPx: number, heightPx: number): number {
  return Math.max(widthPx / heightPx, MIN_PORTRAIT_ASPECT_RATIO)
}
