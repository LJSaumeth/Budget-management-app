export default function HandDrawnDivider() {
  return (
    <svg
      width="100%"
      height="8"
      className="my-2"
      preserveAspectRatio="none"
    >
      <path
        d="M 0,4 Q 50,2 100,5 T 200,3 T 300,6"
        stroke="currentColor"
        strokeWidth="1"
        fill="none"
        className="text-brown/30"
        strokeDasharray="none"
      />
    </svg>
  );
}
