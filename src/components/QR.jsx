import { useEffect, useId } from 'react'
import QRCode from 'qrcode'

console.log(QRCode)

export default function QR(props) {
    const id = useId()

    useEffect(() => {
        QRCode.toCanvas(document.getElementById(id),
                        props.text,
                        {
                            width: props.size,
                            margin: 2,
                            color: {
                                dark: props.dark,
                                light: props.light
                            }
                        },
                        (error => {
                            if (error) {
                                console.error(error)
                            } else {
                                console.log("QR: OK")
                            }
                        }))
        })
        
    return <canvas id={ id } className="qr" style={ {zIndex : 1000} }/>
}
