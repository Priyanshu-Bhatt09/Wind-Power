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
    <div className="min-h-screen bg-black
    flex items-center justify-center
    border-2">
      <div className="border-2 min-h-screen
      my-40 lg:my-20 lg:w-[70vw]
      ">
      {/* top controls  */}
      <div className="flex flex-row gap-2 lg:gap-4 m-2 border-2
      w-[95vw] lg:w-[69vw] md:w-[70vw] sm:w-[70vw]
      
      ">
        {/* start time  */}
        <div className="border-2 flex items-start lg:flex-col">
          <label className="text-white text-sm lg:text-xl m-1">Start Time:</label>
          <input type="date"
          value={startTime}
          onChange={(e) => setStartTime(e.target.value)}
          className="text-white text-xs lg:text-sm p-1 bg-gray-900 rounded-sm shadow-2xl outline-none m-1"
          />
        </div>

        {/* end time  */}
        <div className="border-2 flex items-start lg:flex-col">
          <label className="text-white text-sm lg:text-xl m-1">End Time:</label>
          <input type="date"
          value={endTime} 
          onChange={(e) => setEndTime(e.target.value)}
          className="text-white text-xs lg:text-sm p-1 bg-gray-900 rounded-sm shadow-2xl outline-none m-1"
          />
        </div>

        {/* horizon slider */}
        <div className="border-2 flex items-start lg:flex-col">
          <label className="text-white text-sm lg:text-xl m-1 ">
            <span>Forecast Horizon: </span>
            <span>{horizonHours}h</span>
          </label>
          <input type="range" 
          min="0"
          max="48"
          value={horizonHours}
          onChange={(e) => setHorizonHours(Number(e.target.value))}
          className="m-1 rounded-sm w-30 lg:w-45"
          />
        </div>
      </div>

      {/* charts section  */}
      <div className="h-[50vh] w-[95vw] lg:h-[70vh] lg:w-[69vw]
      border border-gray-100 rounded-lg mx-2
      
      ">
        {loading ? (
          <div className="text-center font-light">Loading Grid data...</div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={windData} margin={{top: 20, right: 20, left: 5, bottom: 20}}>
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
              <Tooltip
              contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)', backgroundColor: 'black'}}
              labelStyle={{ 
              color: 'white', // This makes the timestamp text dark gray
              fontWeight: 'thin',
              marginBottom: '2px'
              }}
              />
              <Legend verticalAlign="top" height={30} />

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
      
    </div>
    </>
  )
}