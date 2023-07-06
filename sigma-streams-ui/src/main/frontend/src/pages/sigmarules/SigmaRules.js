import React from "react";
import { 
  Grid,
  Button } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import MUIDataTable from "mui-datatables";
import { useState } from "react";
import { Link } from "react-router-dom";
import CustomToolbarSelect from "./CustomToolbarSelect";


// components
import PageTitle from "../../components/PageTitle/PageTitle";
import Widget from "../../components/Widget/Widget";
import Table from "../dashboard/components/Table/Table";

// data
import mock from "../dashboard/mock";

var columns = [
  {label: "Title", name: "title"},
  {label: "Description", name: "description"},
  {label: "Author", name: "author"},
  {label: "Product", name: "product"},
  {label: "Service", name: "service"}
];

const datatableData = [
  {"title": "My Title", "description": "My Description", "author": "My Author", "product": "My Product", "service": "My Service"},
  {"title": "My Title2", "description": "My Description2", "author": "My Author2", "product": "My Product2", "service": "My Service2"},
];

const useStyles = makeStyles(theme => ({
  tableOverflow: {
    overflow: 'auto'
  }
}))

export default function SigmaRules() {
  const [title, setTitle] = useState("test");
  
  const handleRowClick = (rowData, rowState) => {
    console.log(rowData, rowState);
  }

  const options= {
    filterType: "checkbox",
    selectableRows: 'single',
    onRowClick: handleRowClick,
  }

  const selectedTitle = () => {
    setTitle("New Title.");
    console.log("in selectedTitle");
  }

  const components = {
    icons: {
    }
  };

  const classes = useStyles();
  return (
    <>
      <PageTitle title="Sigma Rules" />
      <div className="child">
        <Link
          to={{
          pathname: "/app/sigmaruleeditor",
          title
          }}
        >
          <Button 
            variant="contained"
            color="primary"
            size="large" 
            onClick={() => selectedTitle()}>
              Edit Rule
          </Button>
        </Link>
      </div>
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <MUIDataTable
            title="Employee List"
            data={datatableData}
            columns={columns}
            options={options}
            components={components}
          />
        </Grid>
      </Grid>
    </>
  );
}
