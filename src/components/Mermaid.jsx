import React from "react";
import mermaid from "mermaid";

mermaid.initialize({
    startOnLoad: true,
    securityLevel: "loose",
    theme: "dark",
    fontFamily: "monospace"
});

export default class Mermaid extends React.Component {
    componentDidMount() {
        mermaid.contentLoaded()
    }

    render() {
        return <div className="flex justify-center items-center">
            <div
                style={{ width: this.props.width || 500}}
                className="mermaid">
                    { this.props.chart }
            </div>
        </div>
    }
}
