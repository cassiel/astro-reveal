const buildNavBar = () => {
    const location = window.location.pathname
    const tokens = (location == "/" ? [""] : location.split("/"))
    //  console.log(tokens)

    return <div>{ tokens.map((x, i) => {
        const path = (i == 0 ? "/" : tokens.slice(0, i + 1).join("/"))
        const crumb = (i == 0 ? "ROOT" : x)

        return <span key={ i }>{ (i > 0 && <span className="font-bold"> &#x2B95; </span>) }
            <a href={ path }>{ crumb }</a>
        </span>
    }) }</div>
}

export default function BreadcrumbFooter() {
    return <footer className="nav">{ buildNavBar() }</footer>
}
