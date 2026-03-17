import requests
import pandas as pd

def download_elexon_data():
    # Fetching the actual generation
    print("Fetching Actual Wind Generation...")
    actual_url = "https://data.elexon.co.uk/bmrs/api/v1/datasets/FUELHH/stream"
    actual_params = {
        "settlementDateFrom": "2024-01-01",
        "settlementDateTo": "2024-01-31",
        "fuelType": "WIND"
    }
    
    actual_response = requests.get(actual_url, params=actual_params)
    if actual_response.status_code == 200:
        actual_data = actual_response.json()
        df_actual = pd.DataFrame(actual_data)
        df_actual.to_csv("actual_wind_jan2024.csv", index=False)
        print(f"✅ Saved {len(df_actual)} actual records to actual_wind_jan2024.csv")
    else:
        print(f"Failed to fetch actuals: {actual_response.status_code}")

    # Fetching the Forecasted Generation
    print("Fetching Forecasted Wind Generation...")
    forecast_url = "https://data.elexon.co.uk/bmrs/api/v1/datasets/WINDFOR/stream"
    forecast_params = {
        "publishDateTimeFrom": "2024-01-01T00:00:00Z",
        "publishDateTimeTo": "2024-01-31T23:59:59Z"
    }
    
    forecast_response = requests.get(forecast_url, params=forecast_params)
    if forecast_response.status_code == 200:
        forecast_data = forecast_response.json()
        df_forecast = pd.DataFrame(forecast_data)
        df_forecast.to_csv("forecast_wind_jan2024.csv", index=False)
        print(f"✅ Saved {len(df_forecast)} forecast records to forecast_wind_jan2024.csv")
    else:
        print(f"Failed to fetch forecasts: {forecast_response.status_code}")

if __name__ == "__main__":
    download_elexon_data()