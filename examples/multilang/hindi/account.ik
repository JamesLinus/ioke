
use("hindi")

गणना = मूल नकल करना(
  स्थानांतरण = विधि(मात्रा, से: स्व, को:,
    से संतुलन -= मात्रा
    को संतुलन += मात्रा
  )

  प्रिंट = विधि(
    "<गणना नाम: #{नाम} संतुलन: #{संतुलन}>" प्रिंटलाइन
  )
)

श्रीराम = गणना केसाथ(नाम: "श्रीराम", संतुलन: 142.0)
गुरमीत = गणना केसाथ(नाम: "गुरमीत", संतुलन: 45.7)

गणना   स्थानांतरण(23.0, से: श्रीराम, को: गुरमीत)
गणना   स्थानांतरण(10.0, को: श्रीराम, से: गुरमीत)
गुरमीत  स्थानांतरण(57.4, को: श्रीराम)

श्रीराम  प्रिंट
गुरमीत  प्रिंट
