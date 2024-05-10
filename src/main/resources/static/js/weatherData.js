const fetchWeatherData = async(gardenLatitude, gardenLongitude) => {
    try {
        const response = await fetch('/my-gardens/{gardenId}={gardenName}/weather');
        const data = await response.json();
        console.log(data);
        const currentTemp = data.current.temperature_2m;
        console.log(currentTemp)

        return data;
    } catch (error) {
        console.error(error);
    }
}

document.getElementById('dateOfBirth').addEventListener('change',function(event) {
    fetchWeatherData("-43.532055","172.636230");
});