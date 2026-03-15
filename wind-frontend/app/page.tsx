"use client";

import { useEffect, useState } from "react";
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

export default function WindForecastDashboard() {
  const [startTime, setStartTime] = useState('2024-01-01');
  const [endTime, setEndTime] = useState('2024-01-02');
  const [horizonHours, setHorizonHours] = useState(4);
  const [windData, setWindData] = useState([]);
  const [loading, setLoading] = useState(false);

  //fetch data
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try{
        const response = await fetch(`http://localhost:8080/api/wind-data?startTime=${startTime}&endTime=${endTime}&forecastHorizonHours=${horizonHours}`);
        const data = await response.json();
        console.log("Data", data);

        //format time stamp
        const formattedData = data.map((item: any) => ({
          ...item,
          displayTime: new Date(item.timestamp).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit', month: 'short', day: 'numeric', timeZone: 'UTC'})
        }));

        setWindData(formattedData);
      } catch(error) {
        console.log("Failed to fetch wind data", error);
      }
      setLoading(false);
      
    };
    fetchData();
    
  }, [startTime, endTime, horizonHours]);

  return(
    <>
    <div className="min-h-screen bg-white p-8 font-sans">
      {/* top controls  */}
      <div>
        {/* start time  */}
        <div>
          <label>Start Time:</label>
          <input type="date"
          value={startTime}
          onChange={(e) => setStartTime(e.target.value)}
          />
        </div>

        {/* end time  */}
        <div>
          <label>End Time:</label>
          <input type="date"
          value={endTime} 
          onChange={(e) => setEndTime(e.target.value)}
          />
        </div>

        {/* horizon slider */}
        <div>
          <label>
            <span>Forecast Horizon: </span>
            <span>{horizonHours}h</span>
          </label>
          <input type="range" 
          min="0"
          max="48"
          value={horizonHours}
          onChange={(e) => setHorizonHours(Number(e.target.value))}
          />
        </div>
      </div>

      {/* charts section  */}
      <div className="h-[500px] w-full border border-gray-100 shadow-sm rounded-lg p-4">
        {loading ? (
          <div>Loading Grid data...</div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={windData} margin={{top: 20, right: 30, left: 20, bottom: 20}}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#eee" />
              <XAxis
              dataKey="displayTime"
              tick={{fill: '#6b7280', fontSize: 12}}
              tickMargin={10}
              />
              <YAxis
              tick={{fill: '#6b7280', fontSize: 12}}
              label={{value: 'Power (MW)', angle: -90, position: 'insideLeft', fill: '#374151'}}
              />
              <Tooltip />
              <Legend verticalAlign="top" height={36} />

              {/* actual generation */}
              <Line
              type="monotone"
              dataKey="actualGeneration"
              name="Actual"
              stroke="#2563eb"
              strokeWidth={2}
              dot={false}
              />

              {/* forecast generation  */}
              <Line
              type="monotone"
              dataKey="forecastGeneration"
              name="Forecast"
              stroke="#16a34a"
              strokeWidth={2}
              dot={false}
              connectNulls={true}
              />
            </LineChart>
          </ResponsiveContainer>

        )}
      </div>
    </div>
    </>
  )
}