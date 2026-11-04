const form = document.querySelector("#weather-form");
const cityInput = document.querySelector("#city-input");
const searchButton = document.querySelector("#search-button");
const message = document.querySelector("#message");

const elements = {
  city: document.querySelector("#current-city"),
  observedAt: document.querySelector("#observed-at"),
  temperature: document.querySelector("#current-temperature"),
  condition: document.querySelector("#current-condition"),
  humidity: document.querySelector("#current-humidity"),
  wind: document.querySelector("#current-wind"),
  icon: document.querySelector("#current-icon"),
  average: document.querySelector("#average-temperature"),
  hottestTemperature: document.querySelector("#hottest-temperature"),
  hottestTime: document.querySelector("#hottest-time"),
  umbrellaCard: document.querySelector("#umbrella-card"),
  umbrellaIcon: document.querySelector("#umbrella-icon"),
  umbrellaTitle: document.querySelector("#umbrella-title"),
  umbrellaMessage: document.querySelector("#umbrella-message"),
  hourly: document.querySelector("#hourly-forecast"),
  history: document.querySelector("#history-list")
};

form.addEventListener("submit", (event) => {
  event.preventDefault();
  loadWeather(cityInput.value);
});

document.querySelectorAll("[data-city]").forEach((button) => {
  button.addEventListener("click", () => {
    cityInput.value = button.dataset.city;
    loadWeather(button.dataset.city);
  });
});

async function loadWeather(city) {
  const trimmedCity = city.trim();
  if (!trimmedCity) {
    showError("Enter a city name to search.");
    return;
  }

  setLoading(true);
  clearError();

  try {
    const current = await fetchJson(endpoint("current", trimmedCity));
    const [forecast, average, hottest, umbrella, history] = await Promise.all([
      fetchJson(endpoint("forecast", trimmedCity)),
      fetchJson(endpoint("average", trimmedCity)),
      fetchJson(endpoint("hottest", trimmedCity)),
      fetchJson(endpoint("umbrella", trimmedCity)),
      fetchJson(`${endpoint("history", trimmedCity)}&limit=10`)
    ]);

    renderCurrent(current);
    renderStatistics(average, hottest);
    renderUmbrella(umbrella);
    renderForecast(forecast.hourly);
    renderHistory(history);
  } catch (error) {
    showError(error.message);
  } finally {
    setLoading(false);
  }
}

function endpoint(path, city) {
  return `/api/weather/${path}?city=${encodeURIComponent(city)}`;
}

async function fetchJson(url) {
  const response = await fetch(url);
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.message || "Weather data could not be loaded. Please try again.");
  }
  return response.json();
}

function renderCurrent(current) {
  elements.city.textContent = current.city;
  elements.observedAt.textContent = `Observed ${formatDateTime(current.observedAt)}`;
  elements.temperature.textContent = formatTemperature(current.temperature);
  elements.condition.textContent = current.condition;
  elements.humidity.textContent = `${current.humidity}%`;
  elements.wind.textContent = `${current.windSpeed.toFixed(1)} km/h`;
  elements.icon.textContent = conditionIcon(current.condition);
}

function renderStatistics(average, hottest) {
  elements.average.textContent = formatTemperature(average.averageTemperature);
  elements.hottestTemperature.textContent = formatTemperature(hottest.temperature);
  elements.hottestTime.textContent = formatHour(hottest.time);
}

function renderUmbrella(umbrella) {
  elements.umbrellaCard.classList.toggle("rain", umbrella.umbrellaRecommended);
  elements.umbrellaIcon.textContent = umbrella.umbrellaRecommended ? "☂" : "☀";
  elements.umbrellaTitle.textContent = umbrella.umbrellaRecommended
    ? "Keep an umbrella close"
    : "Umbrella optional";
  elements.umbrellaMessage.textContent = umbrella.message;
}

function renderForecast(hourly) {
  elements.hourly.replaceChildren();

  hourly.forEach((hour, index) => {
    const card = document.createElement("article");
    card.className = "hour-card";

    const time = document.createElement("p");
    time.className = "hour-time";
    time.textContent = index === 0 ? "Now" : formatHour(hour.time);

    const icon = document.createElement("div");
    icon.className = "hour-icon";
    icon.textContent = conditionIcon(hour.condition);
    icon.setAttribute("aria-label", hour.condition);

    const temperature = document.createElement("p");
    temperature.className = "hour-temperature";
    temperature.textContent = `${formatTemperature(hour.temperature)}°`;

    const rain = document.createElement("p");
    rain.className = "hour-rain";
    rain.textContent = `${hour.rainProbability}% rain`;

    card.append(time, icon, temperature, rain);
    elements.hourly.append(card);
  });
}

function renderHistory(history) {
  elements.history.replaceChildren();

  if (history.length === 0) {
    const empty = document.createElement("p");
    empty.className = "empty-state";
    empty.textContent = "Search this city to begin building its observation history.";
    elements.history.append(empty);
    return;
  }

  history.forEach((observation) => {
    const row = document.createElement("article");
    row.className = "history-row";

    const identity = document.createElement("div");
    const icon = document.createElement("span");
    icon.className = "history-icon";
    icon.textContent = conditionIcon(observation.condition);

    const label = document.createElement("span");
    const city = document.createElement("strong");
    city.textContent = observation.city;
    const date = document.createElement("small");
    date.textContent = formatDateTime(observation.observedAt);
    label.append(city, date);
    identity.append(icon, label);

    row.append(
      identity,
      historyMetric(`${formatTemperature(observation.temperature)}°C`, observation.condition),
      historyMetric(`${observation.humidity}%`, "Humidity"),
      historyMetric(`${observation.windSpeed.toFixed(1)} km/h`, "Wind")
    );
    elements.history.append(row);
  });
}

function historyMetric(value, labelText) {
  const metric = document.createElement("div");
  metric.className = "history-metric";
  const valueElement = document.createElement("strong");
  valueElement.textContent = value;
  const label = document.createElement("small");
  label.textContent = labelText;
  metric.append(valueElement, label);
  return metric;
}

function conditionIcon(condition) {
  const normalized = condition.toLowerCase();
  if (normalized.includes("thunder")) return "⛈";
  if (normalized.includes("rain") || normalized.includes("drizzle")) return "🌧";
  if (normalized.includes("snow")) return "❄";
  if (normalized.includes("fog")) return "🌫";
  if (normalized.includes("overcast")) return "☁";
  if (normalized.includes("cloud")) return "⛅";
  return "☀";
}

function formatTemperature(value) {
  return Number(value).toFixed(1);
}

function formatHour(value) {
  return new Intl.DateTimeFormat("en", { hour: "numeric", minute: "2-digit" })
    .format(new Date(value));
}

function formatDateTime(value) {
  return new Intl.DateTimeFormat("en", {
    day: "numeric",
    month: "short",
    hour: "numeric",
    minute: "2-digit"
  }).format(new Date(value));
}

function setLoading(loading) {
  searchButton.disabled = loading;
  searchButton.querySelector("span:first-child").textContent = loading
    ? "Loading…"
    : "Search weather";
}

function showError(text) {
  message.textContent = text;
  message.classList.add("visible");
}

function clearError() {
  message.textContent = "";
  message.classList.remove("visible");
}

loadWeather(cityInput.value);
