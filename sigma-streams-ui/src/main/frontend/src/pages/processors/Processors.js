import React, { useState, useEffect } from "react";
import { Grid } from "@material-ui/core";
import MUIDataTable from "mui-datatables";
import { BarChart, Bar, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

// components
import PageTitle from "../../components/PageTitle/PageTitle";
import Widget from "../../components/Widget";

var columns = [
  {label: "Processor ID", name: "applicationId"},
  {label: "Rules", name: "numRules"},
  {label: "Input Topic", name: "appProperties.data_topic"},
  {label: "Output Topic", name: "appProperties.output_topic"},
  {label: "Service", name: "appProperties.sigma_rule_filter_service"},
  {label: "Product", name: "appProperties.sigma_rule_filter_product"},
  {label: "Last Update", name: "sampleTimestampHr"},
  {label: "Records Process", name: "recordsProcessed"},
  {label: "Detections", name: "numMatches"},
  {label: "Status", name: "kafkaStreamsState"}
];

export default function Processors(props) {
  const [tableData, setTableData] = useState([])
  const SERVER_ENDPOINT = process.env.REACT_APP_SERVER_ENDPOINT;

  const refreshTable = async () => {
    try {
        const data = await (await fetch(SERVER_ENDPOINT + "processorStates")).json();
        //const updatedData = data.map(res=> ({...res, status: getStatus(res)}));

        setTableData(data);
    } catch (err) {
        console.log(err.message)
    }
  }

  useEffect(() => {
    const interval = setInterval(() => {
      refreshTable();
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    refreshTable();
  }, []);

  const options= {
    filterType: "checkbox",
    selectableRows: 'single',
    enableNestedDataAccess: '.',
  }

  return (
    <>
      <PageTitle title="Sigma Processors" />
      <Grid container spacing={4}>
        <Grid item xs={12}>
        <Widget>
        <ResponsiveContainer width="100%" minWidth={500} height={350}>
          <BarChart
            width={500}
            height={300}
            data={tableData}
            margin={{
              top: 5,
              right: 30,
              left: 20,
              bottom: 5,
            }}
          >
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="applicationId" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="recordsProcessed" name="Records Processed" fill="#8884d8" />
            <Bar dataKey="numMatches" name="Detection Count" fill="#82ca9d" />
          </BarChart>
        </ResponsiveContainer>
        </Widget>  
        </Grid>
      </Grid>
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <MUIDataTable
            title=""
            data={tableData}
            columns={columns}
            options={options}
          />
        </Grid>
      </Grid>
    </>
  );
}
