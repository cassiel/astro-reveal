import { useState, useEffect } from 'react'

export default function Clock() {
    // Despite example code, we can't safely do the Date() call here, only in the effect.
    let t = ""
    const [theTime, setTime] = useState(t)
    const [theHue, setHue] = useState(0)

    const update = () => {
        t = new Date().toLocaleTimeString()
        setTime(t)
        setHue(Math.random() * 360)
    }

    useEffect(() => {
        const id = setInterval(update, 1000) //  Sync colour and numeric update - but might slowly slip.
        update()
        return () => clearInterval(id)              //  Shutdown callback.
    }, [])

    return <span className="font-bold" style={{ color: `hsl(${theHue}, 100%, 80%)` }}>{ theTime }</span>
}
