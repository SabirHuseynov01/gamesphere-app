import { useState } from "react";
import { Gamepad2 } from "lucide-react";

export default function MediaImage({ src, alt, fallbackLabel, className = "", loading = "lazy" }) {
    const [failed, setFailed] = useState(false);

    if (!src || failed) {
        return <div className={`${className} media-image-fallback`} role="img" aria-label={fallbackLabel || alt}>
            <Gamepad2 size={34} />
            <span>{fallbackLabel || alt}</span>
        </div>;
    }

    return <img className={className} src={src} alt={alt} loading={loading} onError={() => setFailed(true)} />;
}
